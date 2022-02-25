package no.nav.yrkesskade.ysmeldingapi.client.altinn

import no.nav.yrkesskade.ysmeldingapi.test.AbstractIT
import no.nav.yrkesskade.ysmeldingapi.test.TestMockServerInitialization
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.ContextConfiguration

@SpringBootTest
@ContextConfiguration(initializers = [TestMockServerInitialization::class])
internal class AltinnClientIT : AbstractIT() {

    @Autowired
    lateinit var altinnClient: AltinnClient

    @Test
    fun `hent roller for en person og organisasjon`() {
        val roller = altinnClient.hentRoller("12345678910", "test", "nb")
        assertThat(roller).isNotNull()
    }

    @Test
    fun `hent rettigheter for en person og organisasjon`() {
        val rettigheter = altinnClient.hentRettigheter("fnr", "test")
        assertThat(rettigheter).isNotNull();
    }
}