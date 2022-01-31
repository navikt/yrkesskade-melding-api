package no.nav.yrkesskade.ysmeldingapi.api

import no.nav.yrkesskade.ysmeldingapi.model.Skademelding
import org.springframework.http.ResponseEntity
import org.springframework.stereotype.Component

@Component
class SkademeldingApiDelegateImpl : SkademeldingApiDelegate {

    override fun sendSkademelding(skademelding: Skademelding): ResponseEntity<Unit> {
        println("test")
        return super.sendSkademelding(skademelding)
    }
}