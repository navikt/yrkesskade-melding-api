package no.nav.yrkesskade.ysmeldingapi.services

import com.fasterxml.jackson.databind.JsonNode
import no.nav.yrkesskade.ysmeldingapi.clients.MottakClient
import no.nav.yrkesskade.ysmeldingapi.models.SkademeldingDto
import no.nav.yrkesskade.ysmeldingapi.repositories.SkademeldingRepository
import org.slf4j.LoggerFactory
import org.springframework.http.ResponseEntity
import org.springframework.stereotype.Service
import java.lang.invoke.MethodHandles

@Service
class SkademeldingService(private val mottakClient: MottakClient,
                          private val skademeldingRepository: SkademeldingRepository) {

    private val log = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass())

    fun sendTilMottak(skademeldingDto: SkademeldingDto): SkademeldingDto {
        return mottakClient.sendTilMottak(skademeldingDto).also {
            log.info("Lagret skademelding $it i mottak")
        }
    }

    fun lagreSkademelding(skademelding: JsonNode): Int {
        val skademeldingTilLagring = SkademeldingDto(skademelding = skademelding)
        val lagretSkademelding = skademeldingRepository.save(skademeldingTilLagring.toSkademelding())
        return lagretSkademelding.id!!
    }

    fun hentAlleSkademeldinger(): List<SkademeldingDto> {
        return skademeldingRepository.findAll().map { it.toSkademeldingDto() }
    }

    fun hentSkademeldingMedId(id: Int): SkademeldingDto {
        val skademelding = skademeldingRepository.findById(id)
            .orElseThrow { RuntimeException("Fant ikke skademelding med id $id") }

        return skademelding.toSkademeldingDto()
    }
}