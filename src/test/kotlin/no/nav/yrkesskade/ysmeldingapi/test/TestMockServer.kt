package no.nav.yrkesskade.ysmeldingapi.test

import no.nav.yrkesskade.ysmeldingapi.mockserver.AbstractMockSever
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.stereotype.Component

@Component
@ConditionalOnProperty(
    value = arrayOf("mock.enabled"),
    havingValue = "true",
    matchIfMissing = false
)
class TestMockServer() : AbstractMockSever(null)