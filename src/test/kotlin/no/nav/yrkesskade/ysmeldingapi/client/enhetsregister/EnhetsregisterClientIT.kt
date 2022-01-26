package no.nav.yrkesskade.ysmeldingapi.client.enhetsregister

import no.nav.yrkesskade.ysmeldingapi.models.EnhetsregisterOrganisasjonDto
import no.nav.yrkesskade.ysmeldingapi.test.AbstractIT
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest

@SpringBootTest
class EnhetsregisterClientIT: AbstractIT() {

    @Autowired lateinit var enhetsregisterClient: EnhetsregisterClient

    @Test
    fun `hent organisasjon fra enhetsregister som finnes`() {
        val organisasjon: EnhetsregisterOrganisasjonDto = enhetsregisterClient.hentOrganisasjonFraEnhetsregisteret("910720120", false)

        assertThat(organisasjon).isNotNull()
        assertThat(organisasjon.naering?.kode).isNotNull()
    }

    @Test
    fun `hent organisasjon fra enhetsregister som ikke finnes`() {
        val organisasjon: EnhetsregisterOrganisasjonDto = enhetsregisterClient.hentOrganisasjonFraEnhetsregisteret("910720121", false)

        // dersom enhetsregisteret returner 404 (NotFound) - vil klienten lage ett tomt objekt
        assertThat(organisasjon).isNotNull()
        assertThat(organisasjon.navn).isNull()
        assertThat(organisasjon.naering?.kode).isNull()
    }
}