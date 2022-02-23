package no.nav.yrkesskade.ysmeldingapi.api

import no.nav.yrkesskade.model.SkademeldingMetadata
import no.nav.yrkesskade.model.Spraak
import no.nav.yrkesskade.skademelding.api.SkademeldingApiDelegate
import no.nav.yrkesskade.skademelding.model.Skademelding
import no.nav.yrkesskade.ysmeldingapi.services.SkademeldingService
import no.nav.yrkesskade.ysmeldingapi.utils.AutentisertBruker
import org.springframework.http.ResponseEntity
import org.springframework.stereotype.Component
import org.springframework.web.servlet.support.ServletUriComponentsBuilder
import java.time.Instant
import javax.servlet.http.HttpServletRequest

@Component
class SkademeldingApiDelegateImpl(
    private val skademeldingService: SkademeldingService,
    private val autentisertBruker: AutentisertBruker,
    private val httpServletRequest: HttpServletRequest
    ) : SkademeldingApiDelegate {

    override fun sendSkademelding(skademelding: Skademelding): ResponseEntity<Unit> {
        val skademeldingMetadata = SkademeldingMetadata(
            tidspunktMottatt = Instant.now(),
            kilde = httpServletRequest.getHeader("x-nav-ys-kilde") ?: "ukjent",
            spraak = Spraak.NB
        )
        val lagretSkademeldingDto = skademeldingService.lagreSkademelding(skademelding, skademeldingMetadata)
        val location = ServletUriComponentsBuilder
            .fromCurrentRequest()
            .path("/{id}")
            .buildAndExpand(lagretSkademeldingDto.id)
            .toUri()

        return ResponseEntity.created(location).build()
    }
}