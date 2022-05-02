package no.nav.yrkesskade.ysmeldingapi.services

import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import no.nav.yrkesskade.model.SkademeldingBeriketData
import no.nav.yrkesskade.model.SkademeldingInnsendtHendelse
import no.nav.yrkesskade.model.SkademeldingMetadata
import no.nav.yrkesskade.skademelding.model.Skademelding
import no.nav.yrkesskade.ysmeldingapi.client.mottak.SkademeldingInnsendingClient
import no.nav.yrkesskade.ysmeldingapi.metric.MetricService
import no.nav.yrkesskade.ysmeldingapi.models.SkademeldingDto
import no.nav.yrkesskade.ysmeldingapi.repositories.SkademeldingRepository
import no.nav.yrkesskade.ysmeldingapi.utils.KodeverkValidator
import no.nav.yrkesskade.ysmeldingapi.utils.getLogger
import no.nav.yrkesskade.ysmeldingapi.utils.getSecureLogger
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.lang.invoke.MethodHandles

@Service
class SkademeldingService(private val skademeldingInnsendingClient: SkademeldingInnsendingClient,
                          private val skademeldingRepository: SkademeldingRepository,
                          private val metricService: MetricService,
                          private val kodeverkValidator: KodeverkValidator
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
        skademeldingMetadata: SkademeldingMetadata,
        skademeldingBeriketData: SkademeldingBeriketData
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

    private fun validerSkademelding(skademelding: Skademelding) {
        // Hent kodelister basert på rolletype som skal benyttes for å finne gyldige verdier
        check(skademelding.skadelidt!!.dekningsforhold.rolletype == null, { "rolletype er påkrevd" })

        val rolletype = skademelding.skadelidt!!.dekningsforhold.rolletype.value

        kodeverkValidator.sjekkGyldigKodeverkverdiForType(rolletype, "rolletype", "${rolletype} er ikke en gyldig rolletype kode i kodelisten")

        // felter som skal valideres
        val kodelisteOgVerdi = mutableListOf<Pair<String, String>>(
            Pair("alvorlighetsgrad", skademelding.skade.alvorlighetsgrad.value),
            Pair("harSkadelidtHattFravaer", skademelding.skade.antattSykefravaerTabellH.value),
            Pair("hvorSkjeddeUlykken", skademelding.hendelsesfakta.hvorSkjeddeUlykken.value),
            Pair("tidsrom", skademelding.hendelsesfakta.naarSkjeddeUlykken.value),
            Pair("typeArbeidsplass", skademelding.hendelsesfakta.stedsbeskrivelseTabellF.value),
        )

        skademelding.skade.skadedeDeler.forEach {
            kodelisteOgVerdi.add(Pair("skadetype", it.skadeartTabellC.value))
            kodelisteOgVerdi.add(Pair("skadetKroppsdel", it.kroppsdelTabellD.value))
        }

        skademelding.hendelsesfakta.aarsakUlykkeTabellAogE.forEach {
            kodelisteOgVerdi.add(Pair("aarsakOgBakgrunn", it.value))
        }

        skademelding.hendelsesfakta.bakgrunnsaarsakTabellBogG.forEach {
            kodelisteOgVerdi.add(Pair("bakgrunnForHendelsen", it.value))
        }

        if (skademelding.hendelsesfakta.ulykkessted.adresse != null) {
            Pair("landkoderISO2", skademelding.hendelsesfakta.ulykkessted.adresse!!.land)
        }

        // rolletype benyttes som kategori navn (elev, arbeidstaker, laerling osv)
        kodelisteOgVerdi.forEach {
            kodeverkValidator.sjekkGyldigKodeverkverdiForTypeOgKategori(it.second, it.first, rolletype, "${it.second} er ikke en gyldig ${it.first} verdi. Sjekk kodeliste for gyldige verdier")
        }
    }
}