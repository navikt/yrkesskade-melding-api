package no.nav.yrkesskade.ysmeldingapi.controllers

import no.nav.yrkesskade.ysmeldingapi.test.AbstractIT
import no.nav.yrkesskade.ysmeldingapi.test.TestMockServerInitialization
import org.junit.jupiter.api.*
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.http.HttpHeaders.AUTHORIZATION
import org.springframework.http.MediaType
import org.springframework.test.context.ContextConfiguration
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
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
        )
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.fnr").value("12345678910"))
            .andExpect(jsonPath("$.organisasjoner").isArray)
            .andExpect(jsonPath("$.organisasjoner[?(@.organisasjonsnummer == \"910720120\" && @.naeringskode == \"52.292\")]").exists())
            .andExpect(jsonPath("$.organisasjoner[?(@.organisasjonsnummer == \"810771852\" && @.naeringskode == null && @.navn == \"STOL PÅ TORE\")]").exists())
            .andExpect(jsonPath("\$.organisasjoner[?(@.organisasjonsnummer != null && @.navn != null && @.type != null && @.status != null && @.organisasjonsform != null)]").exists())

    }


    fun `hent brukerinfo med organisasjoner - uautorisert`() {
        mvc.perform(
            get(USER_INFO_PATH)
                .contentType(MediaType.APPLICATION_JSON)
                .characterEncoding(Charsets.UTF_8)
        ).andExpect(status().isUnauthorized)
    }

    
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