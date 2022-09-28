package no.nav.yrkesskade.ysmeldingapi.controllers.v2

import no.nav.security.token.support.core.api.ProtectedWithClaims
import no.nav.yrkesskade.ysmeldingapi.controllers.v2.model.Skademelding
import no.nav.yrkesskade.ysmeldingapi.utils.ISSUER
import no.nav.yrkesskade.ysmeldingapi.utils.LEVEL
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.servlet.support.ServletUriComponentsBuilder

@ProtectedWithClaims(issuer = ISSUER, claimMap = [LEVEL])
@RestController
@RequestMapping(path = ["/v2/skademeldinger"], produces = [MediaType.APPLICATION_JSON_VALUE])
class SkademeldingController {

    @PostMapping(consumes = [MediaType.APPLICATION_JSON_VALUE])
    fun postArbeidsstedSkademelding(@RequestBody skademelding : Skademelding): ResponseEntity<Unit>  {
        val location = ServletUriComponentsBuilder
            .fromCurrentRequest()
            .path("/{id}")
            .buildAndExpand("100")
            .toUri()

        return ResponseEntity.created(location).build()
    }
}