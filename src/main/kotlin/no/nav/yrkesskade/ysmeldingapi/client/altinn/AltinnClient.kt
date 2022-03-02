package no.nav.yrkesskade.ysmeldingapi.client.altinn

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import no.nav.yrkesskade.ysmeldingapi.models.*
import no.nav.yrkesskade.ysmeldingapi.security.maskinporten.MaskinportenClient
import no.nav.yrkesskade.ysmeldingapi.utils.AutentisertBruker
import org.glassfish.jersey.logging.LoggingFeature
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component
import java.util.logging.Level
import java.util.logging.Logger
import javax.ws.rs.BadRequestException
import javax.ws.rs.client.Client
import javax.ws.rs.client.ClientBuilder
import javax.ws.rs.core.Feature
import javax.ws.rs.core.Response


@Component
class AltinnClient(
    private val autentisertBruker: AutentisertBruker,
    @Value("\${spring.application.name}") val applicationName: String,
    @Value("\${api.client.altinn.url}") val altinnUrl: String,
    @Value("\${api.client.altinn.apiKey}") val altinnApiKey: String,
    private val maskinportenClient: MaskinportenClient
    ) {

    private val restklient: Client
    private val logger = Logger.getLogger(javaClass.name)

    init {
        // legger på default headers på alle kall mot Altinn API
        var altinnClientHeaderRequestFilter = AltinnClientHeaderRequestFilter(altinnApiKey)
        val feature: Feature = LoggingFeature(logger, Level.INFO, null, null)
        restklient = ClientBuilder.newBuilder().register(feature).build()
    }

    fun hentOrganisasjoner(fnr: String): List<AltinnOrganisasjonDto> {
        val path = "/api/serviceowner/reportees"

        val response: Response = restklient.target(altinnUrl)
            .path(path)
            .queryParam("subject", autentisertBruker.fodselsnummer)
            .queryParam("showConsentReportees", "false")
            .request("application/hal+json")
            .header("Authorization", "Bearer ${hentMaskinportenToken().tokenResponse.accessToken}")
            .header("ApiKey", altinnApiKey)
            .get()

        if (response.status == Response.Status.OK.statusCode) {
            val altinnReporteeResponse = response.readEntity(AltinnReporteeResponse::class.java)
            return altinnReporteeResponse.embedded.reportees.filterNot { it.type == "Person" }.map { AltinnOrganisasjonDto.fraAltinnReportee(it) }
        } else {
            throw RuntimeException("Klarte ikke hente roller fra Altinn - Status kode: ${response.status}")
        }
    }

    fun hentRettigheter(fnr: String, organisasjonsnummer: String): AltinnRettighetResponse {
        val path = "/api/serviceowner/authorization/rights"

        val response: Response = restklient.target(altinnUrl)
            .path(path)
            .queryParam("subject", fnr)
            .queryParam("reportee", organisasjonsnummer)
            .request("application/hal+json")
            .header("Authorization", "Bearer ${hentMaskinportenToken().tokenResponse.accessToken}")
            .header("ApiKey", altinnApiKey)
            .get()

        if (response.status == Response.Status.OK.statusCode) {
            return response.readEntity(AltinnRettighetResponse::class.java)
        } else {
            throw RuntimeException("Klarte ikke hente roller fra Altinn - Status kode: ${response.status}")
        }
    }

    fun hentRoller(fnr: String, organisasjonsnummer: String, spraak: String = "nb"): Array<AltinnRolleDto> {
        val path = "/api/serviceowner/authorization/roles"
        val response: Response = restklient.target(altinnUrl)
            .path(path)
            .queryParam("language", spraak)
            .queryParam("subject", fnr)
            .queryParam("reportee", organisasjonsnummer)
            .request("application/hal+json")
            .header("Authorization", "Bearer ${hentMaskinportenToken().tokenResponse.accessToken}")
            .header("ApiKey", altinnApiKey)
            .get()

        if (response.status == Response.Status.OK.statusCode) {
            val resultat = response.readEntity(String::class.java)
            logger.info("deserialize: ${resultat}")
            return jacksonObjectMapper().readValue(resultat, Array<AltinnRolleDto>::class.java)
        } else {
            throw BadRequestException("Klarte ikke hente roller fra Altinn - Status kode: ${response.status}")
        }
    }

    private fun hentMaskinportenToken() = maskinportenClient.hentAccessToken()

}
