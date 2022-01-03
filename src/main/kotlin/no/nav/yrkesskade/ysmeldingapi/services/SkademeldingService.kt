package no.nav.yrkesskade.ysmeldingapi.services

import com.fasterxml.jackson.databind.JsonNode
import no.nav.yrkesskade.ysmeldingapi.clients.MottakClient
import no.nav.yrkesskade.ysmeldingapi.domain.Skademelding
import no.nav.yrkesskade.ysmeldingapi.models.SkademeldingDto
import no.nav.yrkesskade.ysmeldingapi.repositories.SkademeldingRepository
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.lang.invoke.MethodHandles
import java.util.Optional

@Service
class SkademeldingService(private val mottakClient: MottakClient,
                          private val skademeldingRepository: SkademeldingRepository) {

    private val log = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass())

    fun sendTilMottak(skademeldingDto: SkademeldingDto): SkademeldingDto {
        return mottakClient.sendTilMottak(skademeldingDto).also {
            log.info("Lagret skademelding $it i mottak")
        }
    }

    @Transactional
    fun lagreSkademelding(skademelding: JsonNode): SkademeldingDto {
        val skademeldingTilLagring = SkademeldingDto(
            id = skademelding.get("id")?.asInt(),
            skademelding = skademelding.get("skademelding")
        )
        val lagretSkademeldingDto = skademeldingRepository.save(skademeldingTilLagring.toSkademelding())
        return lagretSkademeldingDto.toSkademeldingDto()
    }

    fun hentAlleSkademeldinger(): List<SkademeldingDto> {
        return skademeldingRepository.findAll().map { it.toSkademeldingDto() }
    }

    fun hentSkademeldingMedId(id: Int): Optional<Skademelding> {
        return skademeldingRepository.findById(id)
    }

    @Transactional
    fun slettSkademelding(id: Int) {
        skademeldingRepository.deleteById(id)
    }
}