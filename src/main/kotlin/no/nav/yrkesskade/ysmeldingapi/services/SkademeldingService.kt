package no.nav.yrkesskade.ysmeldingapi.services

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import no.nav.yrkesskade.skademelding.model.Skademelding
import no.nav.yrkesskade.ysmeldingapi.client.mottak.SkademeldingInnsendingClient
import no.nav.yrkesskade.ysmeldingapi.domain.SkademeldingEntity
import no.nav.yrkesskade.ysmeldingapi.models.SkademeldingDto
import no.nav.yrkesskade.ysmeldingapi.models.SkademeldingInnsendtHendelse
import no.nav.yrkesskade.ysmeldingapi.models.SkademeldingMetadata
import no.nav.yrkesskade.ysmeldingapi.repositories.SkademeldingRepository
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.lang.invoke.MethodHandles
import java.util.*

@Service
class SkademeldingService(private val skademeldingInnsendingClient: SkademeldingInnsendingClient,
                          private val skademeldingRepository: SkademeldingRepository
) {

    private val log = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass())

    fun sendTilMottak(skademeldingInnsendtHendelse: SkademeldingInnsendtHendelse): SkademeldingInnsendtHendelse {
        return skademeldingInnsendingClient.sendTilMottak(skademeldingInnsendtHendelse).also {
            log.info("Lagret skademelding $it i mottak")
        }!!
    }

    @Transactional
    fun lagreSkademelding(skademelding: Skademelding, skademeldingMetadata: SkademeldingMetadata): SkademeldingDto {
        val skademeldingTilLagring = SkademeldingDto(
            id = null,
            skademelding = jacksonObjectMapper().valueToTree(skademelding), // konverter til JsonNode
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