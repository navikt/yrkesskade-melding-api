package no.nav.yrkesskade.ysmeldingapi.services

import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import no.bekk.bekkopen.person.FodselsnummerValidator
import no.nav.yrkesskade.model.SkademeldingBeriketData
import no.nav.yrkesskade.model.SkademeldingInnsendtHendelse
import no.nav.yrkesskade.model.SkademeldingMetadata
import no.nav.yrkesskade.model.Systemkilde
import no.nav.yrkesskade.skademelding.model.Periode
import no.nav.yrkesskade.skademelding.model.Skademelding
import no.nav.yrkesskade.skademelding.model.SkadetDel
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
        checkNotNull(skademelding.skadelidt.dekningsforhold.rolletype, { "rolletype er påkrevd" })

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
            Tidstype.periode -> validerTidsperioder(skademelding.hendelsesfakta.tid.perioder)
        }

        if (skademelding.hendelsesfakta.ulykkessted.adresse != null) {
            kodeverkValidator.sjekkGyldigKodeverkverdiForType(skademelding.hendelsesfakta.ulykkessted.adresse!!.land!!,"landkoderISO2", "${skademelding.hendelsesfakta.ulykkessted.adresse!!.land!!} er ikke en gyldig landkode. Sjekk landkoderISO2 for gyldige verdier")
        }

        if (skjematype.harTjenestePeriode) {

        }

        // felter som skal valideres
        val kodelisteOgVerdi = mutableListOf(
            Pair("hvorSkjeddeUlykken", skademelding.hendelsesfakta.hvorSkjeddeUlykken),
            Pair("tidsrom", skademelding.hendelsesfakta.naarSkjeddeUlykken),
        )

        // dersom tidstype er periode - valider sykdomsfelter
        if (skademelding.hendelsesfakta.tid.tidstype == Tidstype.periode) {
            skademelding.hendelsesfakta.paavirkningsform!!.forEach {
                kodeverkValidator.sjekkGyldigKodeverkverdiForType(
                    it,
                    "paavirkningsform",
                    "${it} er ikke en gyldig paavirkningsform verdi. Sjekk kodeliste for gyldige verdier")
            }
        }

        validerSkadedeDeler(skademelding.skade.skadedeDeler)

        if (!erPeriode(skademelding)) {
            checkNotNull(skademelding.hendelsesfakta.aarsakUlykke, { "aarsakUlykke er påkrevd ved yrkesskade"})
            skademelding.hendelsesfakta.aarsakUlykke!!.forEach {
                kodelisteOgVerdi.add(Pair("aarsakOgBakgrunn", it))
            }
        }

        if (!erPeriode(skademelding) && skjematype.harBakgrunnAarsak) {
            checkNotNull(skademelding.hendelsesfakta.bakgrunnsaarsak, { "bakgrunnAarsak er påkrevd ved yrkesskade"})
            skademelding.hendelsesfakta.bakgrunnsaarsak!!.forEach {
                kodelisteOgVerdi.add(Pair("bakgrunnForHendelsen", it))
            }
        }

        if (skademelding.skade.alvorlighetsgrad != null) {
            kodelisteOgVerdi.add(Pair("alvorlighetsgrad", skademelding.skade!!.alvorlighetsgrad!!))
        }

        if (skademelding.skadelidt.dekningsforhold.stillingstittelTilDenSkadelidte != null && skjematype.harStilling && !erPeriode(skademelding)) {
            skademelding.skadelidt.dekningsforhold.stillingstittelTilDenSkadelidte!!.forEach {
                kodelisteOgVerdi.add(Pair("stillingstittel", it))
            }
            kodelisteOgVerdi.add(Pair("harSkadelidtHattFravaer", skademelding.skade.antattSykefravaer!!))
        }
        if (skademelding.hendelsesfakta.stedsbeskrivelse != null && skjematype.harStedsbeskrivelse && !erPeriode(skademelding)) {
            kodelisteOgVerdi.add(Pair("typeArbeidsplass", skademelding.hendelsesfakta.stedsbeskrivelse!!))
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

    private fun erPeriode(skademelding: Skademelding) : Boolean = skademelding.hendelsesfakta.tid.tidstype == Tidstype.periode

    private fun validerTidsperioder(perioder: List<Periode>?) {
        checkNotNull(perioder, { "hendelsesfakta.tid.perioder er påkrevd" })
        perioder.forEach {
            checkNotNull(it.fra, { "hendelsesfakta.tid.perioder[*].fra er påkrevd" })
            checkNotNull(it.til, { "hendelsesfakta.tid.perioder[*].til er påkrevd" })
            check(
                it.fra!!.isBefore(it.til!!) || it.fra!!.isEqual(it.til!!),
                { "fra dato må være før eller sammme som til dato" })

        }
    }

    private fun validerTidspunkt(tidspunkt: OffsetDateTime?) {
        checkNotNull(tidspunkt, {"hendelsesfakta.tid.tidspunkt er påkrevd"})
    }

    private fun validerSkadedeDeler(skadedeDeler: List<SkadetDel>) {
        check(skadedeDeler.isNotEmpty(), {"skadedeDeler kan ikke være tom"})

        skadedeDeler.forEach {
            kodeverkValidator.sjekkGyldigKodeverkverdiForTyper(it.skadeart,"${it.skadeart} er ikke en gyldig verdi. Sjekk kodelistene skadetype og sykdomstype for gyldige verdier", "skadetype", "sykdomstype" )
            kodeverkValidator.sjekkGyldigKodeverkverdiForType(it.kroppsdel, "skadetKroppsdel", "${it.kroppsdel} er ikke en gyldig verdi. Sjekk kodelisten skadetKroppsdel for gyldige verdier")
        }
    }
}