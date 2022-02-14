package no.nav.yrkesskade.ysmeldingapi.client.mottak

import no.nav.yrkesskade.ysmeldingapi.models.SkademeldingDto
import org.springframework.beans.factory.annotation.Value
import org.springframework.http.MediaType
import org.springframework.stereotype.Component
import javax.ws.rs.client.ClientBuilder
import javax.ws.rs.client.Entity
import javax.ws.rs.core.Response

@Component
class MottakClient(@Value("\${api.client.yrkesskade-mottak-url}") private val mottakBaseUrl: String) {

    private val client = ClientBuilder.newClient()

    fun sendTilMottak(skademelding: SkademeldingDto): SkademeldingDto {
        val response = client.target("$mottakBaseUrl/api/skademelding/").request(MediaType.APPLICATION_JSON_VALUE).post(
            Entity.json(skademelding))

        if (response.statusInfo.family != Response.Status.Family.SUCCESSFUL) {
            throw RuntimeException("Unexpected code $response")
        }

        return response.readEntity(SkademeldingDto::class.java)
    }
}