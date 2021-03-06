package no.nav.yrkesskade.ysmeldingapi.controllers

import no.nav.security.token.support.core.api.ProtectedWithClaims
import no.nav.yrkesskade.skademelding.api.SkademeldingApi
import no.nav.yrkesskade.skademelding.api.SkademeldingApiDelegate
import no.nav.yrkesskade.ysmeldingapi.utils.ISSUER
import no.nav.yrkesskade.ysmeldingapi.utils.LEVEL
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import java.util.*

@ProtectedWithClaims(issuer = ISSUER, claimMap = [LEVEL])
@RestController("/")
@RequestMapping("/")
class SkademeldingApiController(
    @org.springframework.beans.factory.annotation.Autowired(required = false) delegate: SkademeldingApiDelegate?
) : SkademeldingApi {
    private val delegate: SkademeldingApiDelegate

    init {
        this.delegate = Optional.ofNullable(delegate).orElse(object : SkademeldingApiDelegate {})
    }

    override fun getDelegate(): SkademeldingApiDelegate = delegate
}