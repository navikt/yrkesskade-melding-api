package no.nav.yrkesskade.ysmeldingapi.client.altinn

import no.nav.yrkesskade.ysmeldingapi.models.*
import no.nav.yrkesskade.ysmeldingapi.security.maskinporten.MaskinportenClient
import no.nav.yrkesskade.ysmeldingapi.utils.AutentisertBruker
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component
import javax.ws.rs.client.Client
import javax.ws.rs.client.ClientBuilder
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

    init {
        // legger på default headers på alle kall mot Altinn API
        var altinnClientHeaderRequestFilter = AltinnClientHeaderRequestFilter(altinnApiKey)
        restklient = ClientBuilder.newClient().register(altinnClientHeaderRequestFilter)
    }

    fun hentOrganisasjoner(fnr: String): List<AltinnOrganisasjonDto> {
        val path = "/api/serviceowner/reportees?subject={subject}&showConsentReportees=false"

        val response: Response = restklient.target(altinnUrl)
            .path(path)
            .resolveTemplate("subject", autentisertBruker.fodselsnummer)
            .request("application/hal+json")
            .header("Authorization", "Bearer ${hentMaskinportenToken().tokenResponse.accessToken}")
            .get()

        if (response.status == Response.Status.OK.statusCode) {
            val altinnReporteeResponse = response.readEntity(AltinnReporteeResponse::class.java)
            return altinnReporteeResponse.embedded.reportees.filterNot { it.type == "Person" }.map { AltinnOrganisasjonDto.fraAltinnReportee(it) }
        } else {
            throw RuntimeException("Klarte ikke hente roller fra Altinn - Status kode: ${response.status} ${response.}")
        }
    }

    fun hentRettigheter(fnr: String, organisasjonsnummer: String): AltinnRettighetResponse {
        val path = "/api/serviceowner/authorization/rights?subject={subject}&reportee={reportee}"

        val response: Response = restklient.target(altinnUrl)
            .path(path)
            .resolveTemplate("subject", fnr)
            .resolveTemplate("reportee", organisasjonsnummer)
            .request("application/hal+json")
            .header("Authorization", "Bearer ${hentMaskinportenToken().tokenResponse.accessToken}")
            .get()

        if (response.status == Response.Status.OK.statusCode) {
            return response.readEntity(AltinnRettighetResponse::class.java)
        } else {
            throw RuntimeException("Klarte ikke hente roller fra Altinn - Status kode: ${response.status}")
        }
    }

    fun hentRoller(fnr: String, organisasjonsnummer: String, spraak: String = "nb"): AltinnRollerDto {
        val path = "/api/serviceowner/authorization/roles?language={language}&subject={subject}&reportee={reportee}"
        val response: Response = restklient.target(altinnUrl)
            .path(path)
            .resolveTemplate("language", spraak)
            .resolveTemplate("subject", fnr)
            .resolveTemplate("reportee", organisasjonsnummer)
            .request("application/hal+json")
            .header("Authorization", "Bearer ${hentMaskinportenToken().tokenResponse.accessToken}")
            .get()

        if (response.status == Response.Status.OK.statusCode) {
            return response.readEntity(AltinnRollerResponse::class.java).melding
        } else {
            throw RuntimeException("Klarte ikke hente roller fra Altinn - Status kode: ${response.status}")
        }
    }

    private fun hentMaskinportenToken() = maskinportenClient.hentAccessToken()

}
