package no.nav.yrkesskade.ysmeldingapi.controllers

import com.fasterxml.jackson.databind.JsonNode
import no.nav.yrkesskade.ysmeldingapi.models.SkademeldingDto
import no.nav.yrkesskade.ysmeldingapi.services.SkademeldingService
import org.slf4j.LoggerFactory
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import java.lang.invoke.MethodHandles

@RestController
@RequestMapping(path = ["/api/"], produces = [MediaType.APPLICATION_JSON_VALUE])
class SkademeldingController(private val skademeldingService: SkademeldingService) {
    private val log = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass())

    @PostMapping("/skademeldinger")
    fun behandleSkademelding(@RequestBody(required = true) skademeldingDto: SkademeldingDto): ResponseEntity<SkademeldingDto> {
        return ResponseEntity.ok().body(skademeldingService.sendTilMottak(skademeldingDto))
    }

    @PostMapping("/midlertidig/skademeldinger")
    fun mottaSkademelding(@RequestBody(required = true) skademelding: JsonNode): ResponseEntity<Int> {
        val lagretSkademeldingId = skademeldingService.lagreSkademelding(skademelding)
        return ResponseEntity.ok().body(lagretSkademeldingId)
    }

    @GetMapping("/midlertidig/skademeldinger")
    fun hentSkademeldinger(): ResponseEntity<List<SkademeldingDto>> {
        val skademeldinger = skademeldingService.hentAlleSkademeldinger()
        return ResponseEntity.ok().body(skademeldinger)
    }

    @GetMapping("/midlertidig/skademeldinger/{id}")
    fun hentSkademeldingMedId(@PathVariable id: Int): ResponseEntity<SkademeldingDto> {
        val skademelding = skademeldingService.hentSkademeldingMedId(id)
        return ResponseEntity.ok().body(skademelding)
    }
}