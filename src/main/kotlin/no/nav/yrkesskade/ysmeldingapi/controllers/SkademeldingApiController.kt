package no.nav.yrkesskade.ysmeldingapi.controllers

import no.nav.yrkesskade.ysmeldingapi.api.SkademeldingApi
import no.nav.yrkesskade.ysmeldingapi.api.SkademeldingApiDelegate
import org.springframework.web.bind.annotation.RestController
import java.util.*

@RestController("/api")
class SkademeldingApiController(
    @org.springframework.beans.factory.annotation.Autowired(required = false) delegate: SkademeldingApiDelegate?
) : SkademeldingApi {
    private val delegate: SkademeldingApiDelegate

    init {
        this.delegate = Optional.ofNullable(delegate).orElse(object : SkademeldingApiDelegate {})
    }

    override fun getDelegate(): SkademeldingApiDelegate = delegate
}