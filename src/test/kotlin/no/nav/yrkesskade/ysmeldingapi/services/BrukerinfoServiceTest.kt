package no.nav.yrkesskade.ysmeldingapi.services

import no.nav.yrkesskade.ysmeldingapi.client.altinn.AltinnClient
import no.nav.yrkesskade.ysmeldingapi.client.enhetsregister.EnhetsregisterClient
import no.nav.yrkesskade.ysmeldingapi.models.AltinnOrganisasjonDto
import no.nav.yrkesskade.ysmeldingapi.models.EnhetsregisterOrganisasjonDto
import no.nav.yrkesskade.ysmeldingapi.test.AbstractIT
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.ArgumentMatchers.anyString
import org.mockito.Mockito
import org.mockito.Mockito.`when`
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.mock.mockito.MockBean

@SpringBootTest
class BrukerinfoServiceTest : AbstractIT() {

    @Autowired
    lateinit var brukerinfoService: BrukerinfoService

    @MockBean
    lateinit var altinnClient: AltinnClient

    @MockBean
    lateinit var enhetsregisterClient: EnhetsregisterClient

    @BeforeEach
    fun setup() {
        Mockito.reset(altinnClient, enhetsregisterClient);
    }

    @Test
    fun `hent organisasjoner for foedselsnummer uten altinn organisasjoner`() {
        `when`(altinnClient.hentOrganisasjoner(anyString())).thenReturn(emptyList());

        val organisasjoner = brukerinfoService.hentOrganisasjonerForFodselsnummer("12345678910")
        assertThat(organisasjoner.isEmpty()).isTrue()
    }

    @Test
    fun `hent organisasjoner for foedselsnummer uten treff i enhetsregister`() {
        val altinnOrganisasjoner = listOf(AltinnOrganisasjonDto(organisasjonsnummer = "1234556789", organisasjonsform = "test"))
        `when`(altinnClient.hentOrganisasjoner(anyString())).thenReturn(altinnOrganisasjoner);
        `when`(enhetsregisterClient.hentEnhetEllerUnderenhetFraEnhetsregisteret(anyString())).thenReturn(
            EnhetsregisterOrganisasjonDto()
        )

        val organisasjoner = brukerinfoService.hentOrganisasjonerForFodselsnummer("12345678910")
        assertThat(organisasjoner.isEmpty()).isFalse() // skal legge til en tom organisasjon
    }
}