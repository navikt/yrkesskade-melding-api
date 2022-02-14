package no.nav.yrkesskade.ysmeldingapi.client.altinn

import no.nav.arbeidsgiver.altinnrettigheter.proxy.klient.AltinnConfig
import no.nav.arbeidsgiver.altinnrettigheter.proxy.klient.AltinnrettigheterProxyKlient
import no.nav.arbeidsgiver.altinnrettigheter.proxy.klient.AltinnrettigheterProxyKlientConfig
import no.nav.arbeidsgiver.altinnrettigheter.proxy.klient.ProxyConfig
import no.nav.arbeidsgiver.altinnrettigheter.proxy.klient.model.AltinnReportee
import no.nav.arbeidsgiver.altinnrettigheter.proxy.klient.model.SelvbetjeningToken
import no.nav.arbeidsgiver.altinnrettigheter.proxy.klient.model.Subject
import no.nav.yrkesskade.ysmeldingapi.models.AltinnOrganisasjonDto
import no.nav.yrkesskade.ysmeldingapi.models.AltinnRettighetResponse
import no.nav.yrkesskade.ysmeldingapi.models.AltinnRollerDto
import no.nav.yrkesskade.ysmeldingapi.models.AltinnRollerResponse
import no.nav.yrkesskade.ysmeldingapi.security.maskinporten.MaskinportenClient
import no.nav.yrkesskade.ysmeldingapi.utils.AutentisertBruker
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component
import java.util.*
import javax.ws.rs.client.Client
import javax.ws.rs.client.ClientBuilder
import javax.ws.rs.core.Response

@Component
class AltinnClient(
    private val autentisertBruker: AutentisertBruker,
    @Value("\${spring.application.name}") val applicationName: String,
    @Value("\${api.client.altinn.proxyUrl}") val proxyUrl: String,
    @Value("\${api.client.altinn.fallbackUrl}") val fallbackUrl: String,
    @Value("\${api.client.altinn.apiKey}") val altinnApiKey: String,
    private val maskinportenClient: MaskinportenClient
    ) {

    private val proxyklient: AltinnrettigheterProxyKlient
    private val restklient: Client

    init {
        val proxyKlientConfig = AltinnrettigheterProxyKlientConfig(
            ProxyConfig(applicationName, proxyUrl),
            AltinnConfig(fallbackUrl, altinnApiKey, "")
        )
        proxyklient = AltinnrettigheterProxyKlient(proxyKlientConfig)

        // legger på default headers på alle kall mot Altinn API
        var altinnClientHeaderRequestFilter = AltinnClientHeaderRequestFilter(altinnApiKey, maskinportenClient)
        restklient = ClientBuilder.newClient().register(altinnClientHeaderRequestFilter)
    }

    fun hentOrganisasjoner(fnr: String): List<AltinnOrganisasjonDto> =
        run {
            proxyklient.hentOrganisasjoner(
                SelvbetjeningToken(hentMaskinportenToken().tokenResponse.accessToken),
                Subject(fnr),
                true
            )
        }

    private fun run(action: () -> List<AltinnReportee>): List<AltinnOrganisasjonDto> =
        try {
            action().map { AltinnOrganisasjonDto.fraAltinnReportee(it) }
        } catch (error: Exception) {
            if (error.message?.contains("403") == true) {
                Collections.emptyList()
            } else {
                throw RuntimeException("Klarte ikke hente organisasjoner fra Altinn")
            }
        }

    fun hentRettigheter(fnr: String, organisasjonsnummer: String): AltinnRettighetResponse {
        val path = "/api/serviceowner/authorization/rights?ForceEIAuthentication&subject={subject}&reportee={reportee}"

        val response: Response = restklient.target(fallbackUrl)
            .path(path)
            .resolveTemplate("subject", fnr)
            .resolveTemplate("reportee", organisasjonsnummer)
            .request()
            .get()

        if (response.status == Response.Status.OK.statusCode) {
            return response.readEntity(AltinnRettighetResponse::class.java)
        } else {
            throw RuntimeException("Klarte ikke hente roller fra Altinn")
        }
    }

    fun hentRoller(fnr: String, organisasjonsnummer: String, spraak: String = "nb"): AltinnRollerDto {
        val path = "/api/serviceowner/authorization/roles?ForceEIAuthentication&language={language}&subject={subject}&reportee={reportee}"
        val response: Response = restklient.target(fallbackUrl)
            .path(path)
            .resolveTemplate("language", spraak)
            .resolveTemplate("subject", fnr)
            .resolveTemplate("reportee", organisasjonsnummer)
            .request()
            .get()

        if (response.status == Response.Status.OK.statusCode) {
            return response.readEntity(AltinnRollerResponse::class.java).melding
        } else {
            throw RuntimeException("Klarte ikke hente roller fra Altinn")
        }
    }

    private fun hentMaskinportenToken() = maskinportenClient.hentAccessToken()

}
