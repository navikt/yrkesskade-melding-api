package no.nav.yrkesskade.ysmeldingapi.controllers.v1

import no.nav.security.token.support.core.api.ProtectedWithClaims
import no.nav.yrkesskade.ysmeldingapi.models.OrganisasjonDto
import no.nav.yrkesskade.ysmeldingapi.models.BrukerinfoDto
import no.nav.yrkesskade.ysmeldingapi.services.BrukerinfoService
import no.nav.yrkesskade.ysmeldingapi.utils.AutentisertBruker
import no.nav.yrkesskade.ysmeldingapi.utils.ISSUER
import no.nav.yrkesskade.ysmeldingapi.utils.LEVEL
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@ProtectedWithClaims(claimMap = [LEVEL])
@RestController
@RequestMapping(path = ["/api/v1/brukerinfo"], produces = [MediaType.APPLICATION_JSON_VALUE])
class BrukerinfoController(private val autentisertBruker: AutentisertBruker, private val brukerinfoService: BrukerinfoService) {

    @GetMapping()
    fun hentUserInfo(): ResponseEntity<BrukerinfoDto> {
        val organisasjoner: List<OrganisasjonDto> = brukerinfoService.hentOrganisasjonerForFodselsnummer(autentisertBruker.fodselsnummer)
        return ResponseEntity.ok(BrukerinfoDto(fnr = autentisertBruker.fodselsnummer, organisasjoner = organisasjoner))
    }
}