package no.nav.yrkesskade.ysmeldingapi.client.altinn

import no.nav.yrkesskade.ysmeldingapi.security.maskinporten.MaskinportenClient
import java.io.IOException
import javax.ws.rs.container.ContainerRequestContext
import javax.ws.rs.container.ContainerResponseContext
import javax.ws.rs.container.ContainerResponseFilter


class AltinnClientHeaderRequestFilter(private val apiKey: String, private val maskinportenClient: MaskinportenClient) : ContainerResponseFilter {

    @Throws(IOException::class)
    override fun filter(requestContext: ContainerRequestContext, responseContext: ContainerResponseContext) {
        responseContext.headers.add("Authorization", "Bearer ${maskinportenClient.hentAccessToken().tokenResponse.accessToken}")
        responseContext.headers.add("ApiKey", apiKey)
    }
}