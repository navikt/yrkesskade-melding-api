package no.nav.yrkesskade.ysmeldingapi.services

import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import no.bekk.bekkopen.person.FodselsnummerValidator
import no.nav.yrkesskade.model.SkademeldingBeriketData
import no.nav.yrkesskade.model.SkademeldingInnsendtHendelse
import no.nav.yrkesskade.model.SkademeldingMetadata
import no.nav.yrkesskade.model.Systemkilde
import no.nav.yrkesskade.skademelding.model.Skademelding
import no.nav.yrkesskade.skademelding.model.TidPeriode
import no.nav.yrkesskade.skademelding.model.Tidstype
import no.nav.yrkesskade.ysmeldingapi.client.enhetsregister.EnhetsregisterClient
import no.nav.yrkesskade.ysmeldingapi.client.mottak.SkademeldingInnsendingClient
import no.nav.yrkesskade.ysmeldingapi.config.FeatureToggleService
import no.nav.yrkesskade.ysmeldingapi.config.FeatureToggles
import no.nav.yrkesskade.ysmeldingapi.metric.MetricService
import no.nav.yrkesskade.ysmeldingapi.models.SkademeldingDto
import no.nav.yrkesskade.ysmeldingapi.models.Skjematype
import no.nav.yrkesskade.ysmeldingapi.repositories.SkademeldingRepository
import no.nav.yrkesskade.ysmeldingapi.utils.KodeverkValidator
import no.nav.yrkesskade.ysmeldingapi.utils.getLogger
import no.nav.yrkesskade.ysmeldingapi.utils.getSecureLogger
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.lang.invoke.MethodHandles
import java.time.OffsetDateTime

@Service
class SkademeldingService(private val skademeldingInnsendingClient: SkademeldingInnsendingClient,
                          private val skademeldingRepository: SkademeldingRepository,
                          private val metricService: MetricService,
                          private val kodeverkValidator: KodeverkValidator,
                          private val enhetsregisterClient: EnhetsregisterClient,
                          private val featureToggleService: FeatureToggleService
) {

    private val log = getLogger(MethodHandles.lookup().lookupClass())
    private val secureLog = getSecureLogger()
    private val objectMapper = jacksonObjectMapper().registerModule(JavaTimeModule())

    fun sendTilMottak(skademeldingInnsendtHendelse: SkademeldingInnsendtHendelse): SkademeldingInnsendtHendelse {
        return skademeldingInnsendingClient.sendTilMottak(skademeldingInnsendtHendelse).also {
            secureLog.info("Sendt skademelding $it til mottak")
            metricService.insertMetrikk(skademeldingInnsendtHendelse)
        }
    }

    @Transactional
    fun lagreSkademelding(
        skademelding: Skademelding,
        skademeldingMetadata: SkademeldingMetadata
    ): SkademeldingDto {
        // Valider skademelding
        validerSkademelding(skademelding)

        // Alt ok
        val skademeldingTilLagring = SkademeldingDto(
            id = null,
            skademelding = objectMapper.valueToTree(skademelding), // konverter til JsonNode
            kilde = skademeldingMetadata.kilde,
            mottattTidspunkt = skademeldingMetadata.tidspunktMottatt
        )

        val skademeldingBeriketData = lagBeriketSkademelding(skademelding)

        // lagre i database - returnerer entity
        val lagretSkademeldingDto = skademeldingRepository.save(skademeldingTilLagring.toSkademelding()).toSkademeldingDto()

        // send til mottak dersom databaselagring er ok
        sendTilMottak(
            SkademeldingInnsendtHendelse(
                skademelding = skademelding,
                metadata = skademeldingMetadata,
                beriketData = skademeldingBeriketData
            )
        )

        // returner lagrede skademelding
        return lagretSkademeldingDto
    }

    private fun lagBeriketSkademelding(skademelding: Skademelding): SkademeldingBeriketData {
        val innmeldersOrganisasjon = enhetsregisterClient.hentEnhetEllerUnderenhetFraEnhetsregisteret(skademelding.innmelder.paaVegneAv)

        return SkademeldingBeriketData(
            innmeldersOrganisasjonsnavn = innmeldersOrganisasjon.navn.orEmpty() to Systemkilde.ENHETSREGISTERET
        )
    }

    private fun validerSkademelding(skademelding: Skademelding) {
        // null sjekk
        checkNotNull(skademelding.skadelidt, {"skadelidt er påkrevd"})
        checkNotNull(skademelding.hendelsesfakta, {"hendelsesfakta er påkrevd"})
        checkNotNull(skademelding.skade, {"skade er påkrevd"})
        check(skademelding.innmelder.innmelderrolle == "virksomhetsrepresentant", { "${skademelding.innmelder.innmelderrolle} er ikke en gyldig verdi. Må være virksomhetsrepresentant"})

        // Hent kodelister basert på rolletype som skal benyttes for å finne gyldige verdier
        check(skademelding.skadelidt.dekningsforhold.rolletype != null, { "rolletype er påkrevd" })

        val rolletype = skademelding.skadelidt.dekningsforhold.rolletype

        kodeverkValidator.sjekkGyldigKodeverkverdiForType(rolletype, "rolletype", "${rolletype} er ikke en gyldig rolletype kode i kodelisten")
        val skjematype = Skjematype.hentSkjematypeForNavn(rolletype)
        checkNotNull(skjematype, {"Kunne ikke finne skjematype for ${rolletype}"})

        // sjekk organisasjon
        check(!skademelding.innmelder.paaVegneAv.isNullOrBlank(), {"paaVegneAv er påkrevd"})
        check(!skademelding.skadelidt.dekningsforhold.organisasjonsnummer.isNullOrBlank(), {"organisasjonsnummer er påkrevd"})

        val paaVegneAvOrganisasjon = enhetsregisterClient.hentEnhetEllerUnderenhetFraEnhetsregisteret(skademelding.innmelder.paaVegneAv)
        check(!paaVegneAvOrganisasjon.organisasjonsnummer.isNullOrBlank(), {"Ugyldig innmelder.paaVegneAv enhet. ${skademelding.innmelder.paaVegneAv} er finnes ikke i enhetsregisteret"})

        val dekningsforholdOrganisasjon = enhetsregisterClient.hentEnhetEllerUnderenhetFraEnhetsregisteret(skademelding.skadelidt.dekningsforhold.organisasjonsnummer)
        check(!dekningsforholdOrganisasjon.organisasjonsnummer.isNullOrBlank(), {"Ugyldig dekningsforhold.organisasjonsnummer enhet. ${skademelding.skadelidt.dekningsforhold.organisasjonsnummer} finnes ikke i enhetsregisteret"})

        // sjekk fødselsnumre i produksjon
        if (!featureToggleService.isEnabled(FeatureToggles.ER_IKKE_PROD.toggleId)) {
            check(
                FodselsnummerValidator.isValid(skademelding.innmelder.norskIdentitetsnummer),
                { "innmelder.norskIdentitetsnummer er ugyldig. ${skademelding.innmelder.norskIdentitetsnummer} er ikke gyldig norsk person identitetsnummer" })
            check(
                FodselsnummerValidator.isValid(skademelding.skadelidt.norskIdentitetsnummer),
                { "skadelidt.norskIdentitetsnummer er ugyldig. ${skademelding.skadelidt.norskIdentitetsnummer} er ikke gyldig norsk person identitetsnummer" })
        }
        // valider at norskIdentitetsnumre er ulike
        check(!skademelding.innmelder.norskIdentitetsnummer.equals(skademelding.skadelidt.norskIdentitetsnummer),
            {"innsenders norsk identitetsnummer kan ikke være det samme som skadelidtes norske identitetsnummer"})

        // valider tidstype
        checkNotNull(skademelding.hendelsesfakta.tid.tidstype, {"hendelsesfakta.tid.tidstype er påkrevd"})
        when (skademelding.hendelsesfakta.tid.tidstype) {
            Tidstype.tidspunkt -> validerTidspunkt(skademelding.hendelsesfakta.tid.tidspunkt)
            Tidstype.periode -> validerTidsperiode(skademelding.hendelsesfakta.tid.periode)
        }

        // dersom tidstype er periode - valider sykdomsfelter
        if (skademelding.hendelsesfakta.tid.tidstype == Tidstype.periode) {
            kodeverkValidator.sjekkGyldigKodeverkverdiForType(skademelding.skade.sykdomstype.orEmpty(), "sykdomstype", "${skademelding.skade.sykdomstype} er ikke en gyldig verdi")
            kodeverkValidator.sjekkGyldigKodeverkverdiForType(skademelding.hendelsesfakta.paavirkningsform.orEmpty(), "paavirkningsform", "${skademelding.hendelsesfakta.paavirkningsform} er ikke en gyldig verdi")
        }

        if (skademelding.hendelsesfakta.ulykkessted.adresse != null) {
            kodeverkValidator.sjekkGyldigKodeverkverdiForType(skademelding.hendelsesfakta.ulykkessted.adresse!!.land!!,"landkoderISO2", "${skademelding.hendelsesfakta.ulykkessted.adresse!!.land!!} er ikke en gyldig landkode. Sjekk landkoderISO2 for gyldige verdier")
        }

        // felter som skal valideres
        val kodelisteOgVerdi = mutableListOf(
            Pair("hvorSkjeddeUlykken", skademelding.hendelsesfakta.hvorSkjeddeUlykken),
            Pair("tidsrom", skademelding.hendelsesfakta.naarSkjeddeUlykken),
        )

        check(skademelding.skade.skadedeDeler.isNotEmpty(), {"skadedeDeler kan ikke være tom"})
        skademelding.skade.skadedeDeler.forEach {
            kodelisteOgVerdi.add(Pair("skadetype", it.skadeartTabellC))
            kodelisteOgVerdi.add(Pair("skadetKroppsdel", it.kroppsdelTabellD))
        }

        skademelding.hendelsesfakta.aarsakUlykkeTabellAogE.forEach {
            kodelisteOgVerdi.add(Pair("aarsakOgBakgrunn", it))
        }

        skademelding.hendelsesfakta.bakgrunnsaarsakTabellBogG.forEach {
            kodelisteOgVerdi.add(Pair("bakgrunnForHendelsen", it))
        }

        if (skademelding.skade.alvorlighetsgrad != null) {
            kodelisteOgVerdi.add(Pair("alvorlighetsgrad", skademelding.skade!!.alvorlighetsgrad!!))
        }

        if (skademelding.skadelidt.dekningsforhold.stillingstittelTilDenSkadelidte != null && skjematype.harStilling) {
            skademelding.skadelidt.dekningsforhold.stillingstittelTilDenSkadelidte!!.forEach {
                kodelisteOgVerdi.add(Pair("stillingstittel", it))
            }
            kodelisteOgVerdi.add(Pair("harSkadelidtHattFravaer", skademelding.skade.antattSykefravaerTabellH!!))
        }
        if (skademelding.hendelsesfakta.stedsbeskrivelseTabellF != null && skjematype.harStedsbeskrivelse) {
            kodelisteOgVerdi.add(Pair("typeArbeidsplass", skademelding.hendelsesfakta.stedsbeskrivelseTabellF!!))
        }

        // rolletype benyttes som kategori navn (elev, arbeidstaker, laerling osv)
        kodelisteOgVerdi.forEach {
            kodeverkValidator.sjekkGyldigKodeverkverdiForTypeOgKategori(
                it.second,
                it.first,
                rolletype,
                "${it.second} er ikke en gyldig ${it.first} verdi. Sjekk kodeliste for gyldige verdier"
            )
        }

    }

    private fun validerTidsperiode(periode: TidPeriode?) {
        checkNotNull(periode, {"hendelsesfakta.tid.periode er påkrevd"})
        checkNotNull(periode.fra, {"hendelsesfakta.tid.periode.fra er påkrevd"})
        checkNotNull(periode.til, {"hendelsesfakta.tid.periode.til er påkrevd"})
        check(periode.fra!!.isBefore(periode.til!!) || periode.fra!!.isEqual(periode.til!!), {"fra dato må være før eller sammme som til dato"})
        checkNotNull(periode.sykdomPaavist, {"hendelsesfakta.tid.periode.sykdomPaavist er påkrevd"})
    }

    private fun validerTidspunkt(tidspunkt: OffsetDateTime?) {
        checkNotNull(tidspunkt, {"hendelsesfakta.tid.tidspunkt er påkrevd"})
    }
}