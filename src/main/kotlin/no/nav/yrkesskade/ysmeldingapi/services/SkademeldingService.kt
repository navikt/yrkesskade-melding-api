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
import no.nav.yrkesskade.ysmeldingapi.skjema.SkjemaContext
import no.nav.yrkesskade.ysmeldingapi.skjema.SkjemaFactory
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
        val innmeldingsskjema = SkjemaFactory.hentSkjema(SkjemaContext(skademelding, kodeverkValidator, enhetsregisterClient, featureToggleService))
        innmeldingsskjema.valider()
    }
}