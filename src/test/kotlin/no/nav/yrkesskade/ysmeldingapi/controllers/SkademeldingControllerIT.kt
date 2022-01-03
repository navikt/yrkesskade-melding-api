package no.nav.yrkesskade.ysmeldingapi.controllers

import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import no.nav.yrkesskade.ysmeldingapi.fixtures.enkelSkademelding
import no.nav.yrkesskade.ysmeldingapi.models.SkademeldingDto
import no.nav.yrkesskade.ysmeldingapi.repositories.testutils.docker.PostgresDockerContainer
import org.assertj.core.api.Assertions.assertThat
import org.hamcrest.Matchers.`is`
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.http.MediaType
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.header
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import org.springframework.transaction.annotation.Transactional

@Transactional
@SpringBootTest
@ActiveProfiles("integration")
@AutoConfigureMockMvc
class SkademeldingControllerIT {

    @Autowired
    lateinit var mvc: MockMvc

    val objectMapper = jacksonObjectMapper()

    init {
        PostgresDockerContainer.container
    }

    @Test
    fun `mottaSkademelding skal ta imot enkel skademelding`() {
        val enkelSkademeldingDto = enkelSkademelding()
        val respons = postSkademelding(enkelSkademeldingDto)
            .andReturn()

        val responsSomJsonNode: JsonNode = objectMapper.readTree(respons.response.contentAsByteArray)
        assertThat(responsSomJsonNode.get("skademelding")).isEqualTo(enkelSkademeldingDto.skademelding)
    }

    @Test
    fun `hentSkademeldinger henter ut 0 skademeldinger`() {
        mvc.perform(MockMvcRequestBuilders.get(SKADEMELDING_PATH))
            .andExpect(status().isNoContent)
    }

    @Test
    fun `hentSkademeldinger henter ut 2 skademeldinger`() {
        val enkelSkademeldingDto = enkelSkademelding()
        postSkademelding(enkelSkademeldingDto)
            .andReturn().response
        postSkademelding(enkelSkademeldingDto)
            .andReturn().response

        mvc.perform(MockMvcRequestBuilders.get(SKADEMELDING_PATH))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.length()", `is`(2)))
    }

    @Test
    fun `hent skademelding med id skal returnere 200`() {
        val enkelSkademeldingDto = enkelSkademelding()
        val postRespons = postSkademelding(enkelSkademeldingDto)
            .andReturn().response
        val url = postRespons.getHeader("Location")

        val getRespons = mvc.perform(MockMvcRequestBuilders.get(url.orEmpty()))
            .andExpect(status().isOk)
            .andReturn().response

        val getResponsSomJsonNode: JsonNode = objectMapper.readTree(getRespons.contentAsByteArray)
        val postResponsSomJsonNode: JsonNode = objectMapper.readTree(postRespons.contentAsByteArray)
        assertThat(getResponsSomJsonNode).isEqualTo(postResponsSomJsonNode)
    }

    @Test
    fun `hent skademelding med id som ikke finnes skal returnere 404`() {
        mvc.perform(MockMvcRequestBuilders.get("$SKADEMELDING_PATH/999"))
            .andExpect(status().isNotFound)
    }

    @Test
    fun `slett skademelding med id som ikke finnes skal returnere 404`() {
        mvc.perform(MockMvcRequestBuilders.delete("$SKADEMELDING_PATH/999"))
            .andExpect(status().isNotFound)
    }

    @Test
    fun `slett skademelding med id skal returnere 204`() {
        val enkelSkademeldingDto = enkelSkademelding()
        val postRespons = postSkademelding(enkelSkademeldingDto)
            .andReturn().response
        val url = postRespons.getHeader("Location")

        mvc.perform(MockMvcRequestBuilders.delete(url.orEmpty()))
            .andExpect(status().isNoContent)
    }

    private fun postSkademelding(skademeldingDto: SkademeldingDto) =
        mvc.perform(
            MockMvcRequestBuilders.post(SKADEMELDING_PATH)
                .contentType(MediaType.APPLICATION_JSON)
                .characterEncoding(Charsets.UTF_8)
                .content(objectMapper.writeValueAsString(skademeldingDto))
        )
            .andExpect(status().isCreated)
            .andExpect(header().exists("Location"))
            .andExpect(jsonPath("$.id").isNumber)

    companion object {
        private const val SKADEMELDING_PATH = "/api/midlertidig/skademeldinger"
    }
}