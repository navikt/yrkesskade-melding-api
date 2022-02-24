package no.nav.yrkesskade.ysmeldingapi.controllers

import no.nav.yrkesskade.ysmeldingapi.mockserver.FNR_MED_ORGANISASJONER
import no.nav.yrkesskade.ysmeldingapi.mockserver.FNR_MED_ORGANISJON_UTEN_ORGNUMMER
import no.nav.yrkesskade.ysmeldingapi.mockserver.FNR_UTEN_ORGANISASJONER
import no.nav.yrkesskade.ysmeldingapi.test.AbstractIT
import no.nav.yrkesskade.ysmeldingapi.test.TestMockServerInitialization
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.http.HttpHeaders.AUTHORIZATION
import org.springframework.http.MediaType
import org.springframework.test.context.ContextConfiguration
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.*
import org.springframework.transaction.annotation.Transactional

@Transactional
@SpringBootTest
@AutoConfigureMockMvc
@ContextConfiguration(initializers = [TestMockServerInitialization::class])
class BrukerinfoControllerIT: AbstractIT() {

    @Autowired
    lateinit var mvc: MockMvc

    @Test
    fun `hent brukerinfo med organisasjoner - autentisert`() {
        // gyldig JWT
        val jwt = mvc.perform(get("/local/jwt")).andReturn().response.contentAsString

        // Data for eksterne tjenester kommer fra localhost MockServer
        mvc.perform(
            get(USER_INFO_PATH)
                .header(AUTHORIZATION, "Bearer $jwt")
                .contentType(MediaType.APPLICATION_JSON)
                .characterEncoding(Charsets.UTF_8)
        )   .andExpect(status().isOk)
            .andExpect(jsonPath("$.fnr").value(FNR_MED_ORGANISASJONER))
            .andExpect(jsonPath("$.navn").value("ROLF BJØRN"))
            .andExpect(jsonPath("$.organisasjoner").isArray)
            .andExpect(jsonPath("$.organisasjoner[?(@.organisasjonsnummer == \"910521551\" && @.naeringskode == \"52.292\")]").exists())
            .andExpect(jsonPath("$.organisasjoner[?(@.organisasjonsnummer == \"910460048\" && @.naeringskode == \"52.292\")]").exists())
            .andExpect(jsonPath("$.organisasjoner[?(@.organisasjonsnummer == \"910441205\" && @.naeringskode == null && @.navn == \"BARDU OG SØRUM REGNSKAP\")]").exists())
            .andExpect(jsonPath("\$.organisasjoner[?(@.organisasjonsnummer != null && @.navn != null && @.type != null && @.status != null && @.organisasjonsform != null)]").exists())

    }

    @Test
    fun `hent brukerinfo som ikke har organisasjoner - autentisert`() {
        // gyldig JWT
        val jwt = mvc.perform(get("/local/jwt?subject=${FNR_UTEN_ORGANISASJONER}")).andReturn().response.contentAsString

        // Data for eksterne tjenester kommer fra localhost MockServer
        mvc.perform(
            get(USER_INFO_PATH)
                .header(AUTHORIZATION, "Bearer $jwt")
                .contentType(MediaType.APPLICATION_JSON)
                .characterEncoding(Charsets.UTF_8)
        )   .andExpect(status().isOk)
            .andExpect(jsonPath("$.fnr").value(FNR_UTEN_ORGANISASJONER))
            .andExpect(jsonPath("$.navn").value(""))
            .andExpect(jsonPath("$.organisasjoner").isEmpty)

    }

    @Test
    fun `hent brukerinfo med organisasjon uten organisasjonsnummer - autentisert`() {
        // gyldig JWT
        val jwt = mvc.perform(get("/local/jwt?subject=${FNR_MED_ORGANISJON_UTEN_ORGNUMMER}")).andReturn().response.contentAsString

        // Data for eksterne tjenester kommer fra localhost MockServer
        mvc.perform(
            get(USER_INFO_PATH)
                .header(AUTHORIZATION, "Bearer $jwt")
                .contentType(MediaType.APPLICATION_JSON)
                .characterEncoding(Charsets.UTF_8)
        )   .andExpect(status().isBadRequest)
            .andExpect(content().string("Organisasjonsnummer kan ikke være tom"))
    }

    @Test
    fun `hent brukerinfo med organisasjoner - uautorisert`() {
        mvc.perform(
            get(USER_INFO_PATH)
                .contentType(MediaType.APPLICATION_JSON)
                .characterEncoding(Charsets.UTF_8)
        ).andExpect(status().isUnauthorized)
    }

    @Test
    fun `hent organisasjon informasjone en bruker har tilgang til`() {
        // gyldig JWT
        val jwt = mvc.perform(get("/local/jwt")).andReturn().response.contentAsString

        // Data for eksterne tjenester kommer fra localhost MockServer
        mvc.perform(
            get("$USER_INFO_PATH/organisasjoner/90912098")
                .header(AUTHORIZATION, "Bearer $jwt")
                .characterEncoding(Charsets.UTF_8)
                .contentType(MediaType.APPLICATION_JSON)
                .characterEncoding(Charsets.UTF_8)
        ).andExpect(status().isOk)
    }

    @Test
    fun `hent brukerinfo med organisasjoner - ugyldig JWT token`() {
        // token generert fra jwt.io
        val ugyldigJWT = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiIxMjM0NTY3ODkwIiwibmFtZSI6IkpvaG4gRG9lIiwiaWF0IjoxNTE2MjM5MDIyfQ.SflKxwRJSMeKKF2QT4fwpMeJf36POk6yJV_adQssw5c"

        mvc.perform(
            get(USER_INFO_PATH)
                .header(AUTHORIZATION, "Bearer $ugyldigJWT")
                .contentType(MediaType.APPLICATION_JSON)
                .characterEncoding(Charsets.UTF_8)
        )
            .andExpect(status().isUnauthorized)
    }

    companion object {
        private const val USER_INFO_PATH = "/api/v1/brukerinfo"
    }
}