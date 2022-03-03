package no.nav.yrkesskade.ysmeldingapi.client.altinn

import no.nav.yrkesskade.ysmeldingapi.exceptions.AltinnException
import no.nav.yrkesskade.ysmeldingapi.test.AbstractIT
import no.nav.yrkesskade.ysmeldingapi.test.TestMockServerInitialization
import no.nav.yrkesskade.ysmeldingapi.utils.AutentisertBruker
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Test
import org.mockito.Mockito.`when`
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.mock.mockito.MockBean
import org.springframework.test.context.ContextConfiguration


@SpringBootTest
@ContextConfiguration(initializers = [TestMockServerInitialization::class])
internal class AltinnClientIT : AbstractIT() {

    @Autowired
    lateinit var altinnClient: AltinnClient

    @MockBean
    lateinit var autentisertBruker: AutentisertBruker

    @Test
    fun `hent roller for en person og organisasjon`() {
        val roller = altinnClient.hentRoller("12345678910", "test", "nb")
        assertThat(roller).isNotNull()
    }

    @Test
    fun `hent rettigheter for en person og organisasjon`() {
        val rettigheter = altinnClient.hentRettigheter("12345678910", "test")
        assertThat(rettigheter).isNotNull();
    }

    @Test
    fun `hent organisasjoner fra altinn som feiler`() {
        `when`(autentisertBruker.fodselsnummer).thenReturn("1")
        val exception = assertThrows(AltinnException::class.java) {
            altinnClient.hentOrganisasjoner("1")
        }

        assertThat(exception.httpStatus).isEqualTo(400)
    }
}