package no.nav.yrkesskade.ysmeldingapi.services

import no.nav.yrkesskade.ysmeldingapi.clients.MottakClient
import no.nav.yrkesskade.ysmeldingapi.models.SkademeldingDto
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import java.lang.invoke.MethodHandles

@Service
class SkademeldingService(private val mottakClient: MottakClient) {

    private val log = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass())

    fun sendTilMottak(skademeldingDto: SkademeldingDto): SkademeldingDto {
        return mottakClient.sendTilMottak(skademeldingDto).also {
            log.info("Lagret skademelding $it i mottak")
        }
    }
}