package no.nav.yrkesskade.ysmeldingapi.client.enhetsregister

import no.nav.yrkesskade.ysmeldingapi.models.EnhetsregisterOrganisasjonDto
import no.nav.yrkesskade.ysmeldingapi.test.AbstractIT
import no.nav.yrkesskade.ysmeldingapi.test.TestMockServerInitialization
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.ContextConfiguration

@SpringBootTest
@ContextConfiguration(initializers = [TestMockServerInitialization::class])
class EnhetsregisterClientIT : AbstractIT() {

    @Autowired lateinit var enhetsregisterClient: EnhetsregisterClient

    @Test
    fun `hent enhet fra enhetsregister som finnes`() {
        val organisasjon: EnhetsregisterOrganisasjonDto = enhetsregisterClient.hentEnhetFraEnhetsregisteret("910521551")

        assertThat(organisasjon).isNotNull()
        assertThat(organisasjon.naering?.kode).isNotNull()
        assertThat(organisasjon.forretningsadresse).isNotNull()
        assertThat(organisasjon.antallAnsatte).isEqualTo(50)
    }

    @Test
    fun `hent enhet fra enhetsregister som ikke finnes`() {
        val organisasjon: EnhetsregisterOrganisasjonDto = enhetsregisterClient.hentEnhetFraEnhetsregisteret("910437127")

        // dersom enhetsregisteret returner 404 (NotFound) - vil klienten lage ett tomt objekt
        assertThat(organisasjon).isNotNull()
        assertThat(organisasjon.navn).isNull()
        assertThat(organisasjon.naering?.kode).isNull()
    }

    @Test
    fun `hent underenhet fra enhetsregister som finnes`() {
        val organisasjon: EnhetsregisterOrganisasjonDto = enhetsregisterClient.hentUnderenhetFraEnhetsregisteret("910460048")

        assertThat(organisasjon).isNotNull()
        assertThat(organisasjon.organisasjonsnummer).isEqualTo("910460048")
        assertThat(organisasjon.naering?.kode).isNotNull()
        assertThat(organisasjon.forretningsadresse).isNull()
        assertThat(organisasjon.beliggenhetsadresse).isNotNull()
        assertThat(organisasjon.antallAnsatte).isEqualTo(50)
    }

    @Test
    fun `hent underenhet fra enhetsregister som ikke finnes`() {
        val organisasjon: EnhetsregisterOrganisasjonDto = enhetsregisterClient.hentUnderenhetFraEnhetsregisteret("910720121")

        // dersom enhetsregisteret returner 404 (NotFound) - vil klienten lage ett tomt objekt
        assertThat(organisasjon).isNotNull()
        assertThat(organisasjon.navn).isNull()
        assertThat(organisasjon.naering?.kode).isNull()
    }

    @Test
    fun `hent enhet eller underenhet fra enhetsregister som finnes`() {
        val organisasjon: EnhetsregisterOrganisasjonDto =
            enhetsregisterClient.hentEnhetEllerUnderenhetFraEnhetsregisteret("910521551")

        assertThat(organisasjon).isNotNull()
        assertThat(organisasjon.organisasjonsnummer).isEqualTo("910521551")
        assertThat(organisasjon.naering?.kode).isNotNull()
        assertThat(organisasjon.forretningsadresse).isNotNull()
        assertThat(organisasjon.beliggenhetsadresse).isNull()
        assertThat(organisasjon.antallAnsatte).isEqualTo(50)

        val underenhet: EnhetsregisterOrganisasjonDto =
            enhetsregisterClient.hentEnhetEllerUnderenhetFraEnhetsregisteret("910460048")

        assertThat(underenhet).isNotNull()
        assertThat(underenhet.organisasjonsnummer).isEqualTo("910460048")
        assertThat(underenhet.naering?.kode).isEqualTo("52.292")
        assertThat(underenhet.forretningsadresse).isNull()
        assertThat(underenhet.beliggenhetsadresse).isNotNull()
        assertThat(underenhet.antallAnsatte).isEqualTo(50)
    }

    @Test
    fun `hent enhet eller underenhet fra enhetsregister som ikke finnes`() {
        val organisasjon: EnhetsregisterOrganisasjonDto =
            enhetsregisterClient.hentEnhetEllerUnderenhetFraEnhetsregisteret("910437127")

        // dersom enhetsregisteret returner 404 (NotFound) - vil klienten lage ett tomt objekt
        assertThat(organisasjon).isNotNull()
        assertThat(organisasjon.navn).isNull()
        assertThat(organisasjon.naering?.kode).isNull()
    }
}