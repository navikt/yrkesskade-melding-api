package no.nav.yrkesskade.ysmeldingapi.services

import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import no.nav.yrkesskade.model.SkademeldingInnsendtHendelse
import no.nav.yrkesskade.model.SkademeldingMetadata
import no.nav.yrkesskade.skademelding.model.Skademelding
import no.nav.yrkesskade.ysmeldingapi.client.mottak.SkademeldingInnsendingClient
import no.nav.yrkesskade.ysmeldingapi.domain.SkademeldingEntity
import no.nav.yrkesskade.ysmeldingapi.models.SkademeldingDto
import no.nav.yrkesskade.ysmeldingapi.repositories.SkademeldingRepository
import no.nav.yrkesskade.ysmeldingapi.utils.getLogger
import no.nav.yrkesskade.ysmeldingapi.utils.getSecureLogger
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.lang.invoke.MethodHandles
import java.util.*

@Service
class SkademeldingService(private val skademeldingInnsendingClient: SkademeldingInnsendingClient,
                          private val skademeldingRepository: SkademeldingRepository
) {

    private val log = getLogger(MethodHandles.lookup().lookupClass())
    private val secureLog = getSecureLogger()
    private val objectMapper = jacksonObjectMapper().registerModule(JavaTimeModule())

    fun sendTilMottak(skademeldingInnsendtHendelse: SkademeldingInnsendtHendelse): SkademeldingInnsendtHendelse {
        return skademeldingInnsendingClient.sendTilMottak(skademeldingInnsendtHendelse).also {
            secureLog.info("Sendt skademelding $it til mottak")
        }!!
    }

    @Transactional
    fun lagreSkademelding(skademelding: Skademelding, skademeldingMetadata: SkademeldingMetadata): SkademeldingDto {
        val skademeldingTilLagring = SkademeldingDto(
            id = null,
            skademelding = objectMapper.valueToTree(skademelding), // konverter til JsonNode
            kilde = skademeldingMetadata.kilde,
            mottattTidspunkt = skademeldingMetadata.tidspunktMottatt
        )

        // lagre i database - returnerer entity
        val lagretSkademeldingDto = skademeldingRepository.save(skademeldingTilLagring.toSkademelding()).toSkademeldingDto()

        // send til mottak dersom databaselagring er ok
        sendTilMottak(SkademeldingInnsendtHendelse(skademelding = skademelding, metadata = skademeldingMetadata))

        // returner lagrede skademelding
        return lagretSkademeldingDto
    }

    fun hentAlleSkademeldinger(): List<SkademeldingDto> {
        return skademeldingRepository.findAll().map { it.toSkademeldingDto() }
    }

    fun hentSkademeldingMedId(id: Int): Optional<SkademeldingEntity> {
        return skademeldingRepository.findById(id)
    }

    @Transactional
    fun slettSkademelding(id: Int) {
        skademeldingRepository.deleteById(id)
    }
}