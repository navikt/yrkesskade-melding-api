package no.nav.yrkesskade.ysmeldingapi.controllers.v1

import no.nav.security.token.support.core.api.ProtectedWithClaims
import no.nav.yrkesskade.ysmeldingapi.models.AltinnRolleDto
import no.nav.yrkesskade.ysmeldingapi.models.BrukerinfoDto
import no.nav.yrkesskade.ysmeldingapi.models.OrganisasjonDto
import no.nav.yrkesskade.ysmeldingapi.services.BrukerinfoService
import no.nav.yrkesskade.ysmeldingapi.utils.AutentisertBruker
import no.nav.yrkesskade.ysmeldingapi.utils.ISSUER
import no.nav.yrkesskade.ysmeldingapi.utils.LEVEL
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@ProtectedWithClaims(issuer = ISSUER, claimMap = [LEVEL])
@RestController
@RequestMapping(path = ["/api/v1/brukerinfo"], produces = [MediaType.APPLICATION_JSON_VALUE])
class BrukerinfoController(private val autentisertBruker: AutentisertBruker, private val brukerinfoService: BrukerinfoService) {

    @GetMapping()
    fun hentUserInfo(): ResponseEntity<BrukerinfoDto> {
        val organisasjoner: List<OrganisasjonDto> = brukerinfoService.hentOrganisasjonerForFodselsnummer(autentisertBruker.fodselsnummer)
        val altinnRettighetResponse = brukerinfoService.hentSubjectForFodselsnummerOgOrganisasjon(autentisertBruker.fodselsnummer, organisasjoner.firstOrNull())

        return ResponseEntity.ok(
            BrukerinfoDto(
                fnr = autentisertBruker.fodselsnummer,
                navn = altinnRettighetResponse?.person?.navn.orEmpty(),
                organisasjoner = organisasjoner
            )
        )
    }

    @GetMapping("/organisasjoner/{organisasjonsnummer}")
    fun hentOrganisasjon(@PathVariable("organisasjonsnummer") organisasjonsnummer: String) : ResponseEntity<OrganisasjonDto> {
        val organisasjon = brukerinfoService.hentOrganisasjonForBruker(autentisertBruker.fodselsnummer, organisasjonsnummer)
        return ResponseEntity.ok(organisasjon)
    }

    @GetMapping("/organisasjoner/{organisasjonsnummer}/roller")
    fun hentRoller(@PathVariable("organisasjonsnummer") organisasjonsnummer: String) : ResponseEntity<List<AltinnRolleDto>> {
        val roller = brukerinfoService.hentRollerForFodselsnummerOgOrganisasjon(autentisertBruker.fodselsnummer, organisasjonsnummer)
        return ResponseEntity.ok(roller)
    }
}