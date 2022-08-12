package no.nav.yrkesskade.ysmeldingapi.controllers

import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import no.nav.yrkesskade.ysmeldingapi.fixtures.*
import no.nav.yrkesskade.ysmeldingapi.mockserver.FNR_UTEN_ORGANISASJONER
import no.nav.yrkesskade.ysmeldingapi.models.SkademeldingDto
import no.nav.yrkesskade.ysmeldingapi.test.AbstractIT
import no.nav.yrkesskade.ysmeldingapi.test.TestMockServerInitialization
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.http.HttpHeaders
import org.springframework.http.MediaType
import org.springframework.test.context.ContextConfiguration
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders
import org.springframework.test.web.servlet.result.MockMvcResultHandlers
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.*
import org.springframework.transaction.annotation.Transactional
import java.awt.PageAttributes
import java.net.http.HttpHeaders


@Transactional
@SpringBootTest
@AutoConfigureMockMvc
@ContextConfiguration(initializers = [TestMockServerInitialization::class])
class SkademeldingApiControllerIT: AbstractIT() {

    @Autowired
    lateinit var mvc: MockMvc

    val rolletyper = listOf(
        "arbeidstaker",
        "elevEllerStudent",
        "laerling",
        "tiltaksdeltaker",
        "vernepliktigIFoerstegangstjeneste"
    )

    val objectMapper = jacksonObjectMapper().registerModule(JavaTimeModule())

    @Test
    fun `mottaSkademelding skal ta imot enkel skademelding`() {
        // gyldig JWT
        val jwt = mvc.perform(MockMvcRequestBuilders.get("/local/jwt")).andReturn().response.contentAsString
        assertThat(jwt).isNotNull()

        val enkelSkademeldingDto = enkelSkademelding()
        postSkademelding(skademeldingDtoTilString(enkelSkademeldingDto), jwt)
            .andExpect(status().isCreated)
            .andExpect(header().exists("Location"))
    }

    @Test
    fun `mottaSkademelding - autentisert, men manglende rolletilgang`() {
        // gyldig JWT
        val jwt = mvc.perform(MockMvcRequestBuilders.get("/local/jwt?subject=$FNR_UTEN_ORGANISASJONER")).andReturn().response.contentAsString
        assertThat(jwt).isNotNull()

        val enkelSkademeldingDto = enkelSkademelding()
        postSkademelding(skademeldingDtoTilString(enkelSkademeldingDto), jwt)
            .andExpect(status().isForbidden)
            .andExpect(
                jsonPath("$.melding")
                    .value("Bruker har ikke tilgang til å sende skademelding for organisasjon 910521551")
            )
    }

    @Test
    fun `motta skademelding med feil format på stillingstittel`() {
        // gyldig JWT
        val jwt = mvc.perform(MockMvcRequestBuilders.get("/local/jwt")).andReturn().response.contentAsString
        assertThat(jwt).isNotNull()

        postSkademelding(skademeldingMedFeilStillingstittelFormat(), jwt)
            .andExpect(status().isBadRequest)
            .andExpect(jsonPath("$.melding").isNotEmpty)
    }

    @Test
    fun `valider skademelding med periode tidstype hvor fra dato er samme som til dato - mangler sykdomstype, sykdomPaavist og paavirkningsform`() {
        val jwt = mvc.perform(MockMvcRequestBuilders.get("/local/jwt")).andReturn().response.contentAsString
        assertThat(jwt).isNotNull()

        postSkademelding(skademeldingMedPeriodeFraDatoSammeSomTilDato(), jwt)
            .andDo(MockMvcResultHandlers.print())
            .andExpect(status().is4xxClientError)
    }

    @Test
    fun `valider skademelding med periode tidstype`() {
        val jwt = mvc.perform(MockMvcRequestBuilders.get("/local/jwt")).andReturn().response.contentAsString
        assertThat(jwt).isNotNull()

        postSkademelding(skademeldingMedPeriodeOgSykdomsinformasjon(), jwt)
            .andDo(MockMvcResultHandlers.print())
            .andExpect(status().isCreated)
    }

    @Test
    fun `valider skademeldinger`() {
        val jwt = mvc.perform(MockMvcRequestBuilders.get("/local/jwt")).andReturn().response.contentAsString

        rolletyper.forEach{
            val skademelding = skademelding_ok(it, "skade")
            postSkademelding(skademelding, jwt)
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().isCreated)
        }
    }

    @Test
    fun `valider sykdomsmeldinger`() {
        val jwt = mvc.perform(MockMvcRequestBuilders.get("/local/jwt")).andReturn().response.contentAsString

        rolletyper.forEach{
            val skademelding = skademelding_ok(it, "sykdom")
            postSkademelding(skademelding, jwt)
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().isCreated)
        }
    }

    private fun postSkademelding(skademelding: String, token: String) =
        mvc.perform(
            MockMvcRequestBuilders.post(SKADEMELDING_PATH)
                .header(HttpHeaders.AUTHORIZATION, "Bearer $token")
                .contentType(PageAttributes.MediaType.APPLICATION_JSON)
                .characterEncoding(Charsets.UTF_8)
                .content(skademelding)
        )

    private fun skademeldingDtoTilString(skademeldingDto: SkademeldingDto): String =
        objectMapper.writeValueAsString(skademeldingDto.skademelding)

    companion object {
        private const val SKADEMELDING_PATH = "/v1/skademeldinger"
    }
}