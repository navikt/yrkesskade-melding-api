package no.nav.yrkesskade.ysmeldingapi.controllers

import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import no.nav.yrkesskade.ysmeldingapi.fixtures.enkelSkademelding
import no.nav.yrkesskade.ysmeldingapi.models.SkademeldingDto
import no.nav.yrkesskade.ysmeldingapi.test.AbstractIT
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.http.HttpHeaders
import org.springframework.http.MediaType
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders
import org.springframework.test.web.servlet.result.MockMvcResultMatchers

@SpringBootTest
@AutoConfigureMockMvc
class SkademeldingApiControllerIT: AbstractIT() {

    @Autowired
    lateinit var mvc: MockMvc

    val objectMapper = jacksonObjectMapper().registerModule(JavaTimeModule())

    @Test
    fun `mottaSkademelding skal ta imot enkel skademelding`() {
        // gyldig JWT
        val jwt = mvc.perform(MockMvcRequestBuilders.get("/local/jwt")).andReturn().response.contentAsString
        assertThat(jwt).isNotNull()

        val enkelSkademeldingDto = enkelSkademelding()
        postSkademelding(enkelSkademeldingDto, jwt)
    }

    private fun postSkademelding(skademeldingDto: SkademeldingDto, token: String) =
        mvc.perform(
            MockMvcRequestBuilders.post(SKADEMELDING_PATH)
                .header(HttpHeaders.AUTHORIZATION, "Bearer $token")
                .contentType(MediaType.APPLICATION_JSON)
                .characterEncoding(Charsets.UTF_8)
                .content(objectMapper.writeValueAsString(skademeldingDto.skademelding))
        )
            .andExpect(MockMvcResultMatchers.status().isCreated)
            .andExpect(MockMvcResultMatchers.header().exists("Location"))

    companion object {
        private const val SKADEMELDING_PATH = "/api/v1/skademeldinger"
    }
}