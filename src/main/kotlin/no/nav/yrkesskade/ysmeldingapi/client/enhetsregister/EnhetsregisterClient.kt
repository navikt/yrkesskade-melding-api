package no.nav.yrkesskade.ysmeldingapi.client.enhetsregister

import no.nav.yrkesskade.ysmeldingapi.models.EnhetsregisterOrganisasjonDto
import org.springframework.beans.factory.annotation.Value
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Component
import javax.ws.rs.client.Client
import javax.ws.rs.client.ClientBuilder
import javax.ws.rs.core.MediaType
import javax.ws.rs.core.Response

@Component
class EnhetsregisterClient(
    @Value("\${api.client.enhetsregister.url}") val enhetsregisterUrl: String
) {
    private val client: Client = ClientBuilder.newClient()

    fun hentOrganisasjonFraEnhetsregisteret(
        orgnr: String,
        inkluderHistorikk: Boolean
    ): EnhetsregisterOrganisasjonDto {
        return try {
            val response: Response = client.target(enhetsregisterUrl)
                .path(orgnr)
                .request(MediaType.APPLICATION_JSON)
                .get()

            if (response.status == HttpStatus.NOT_FOUND.value()) {
                // dersom det ikke finnes en enhet  i registeret, returneres en tom organisasjon
                return EnhetsregisterOrganisasjonDto()
            }

            return response.readEntity(EnhetsregisterOrganisasjonDto::class.java)
        } catch (e: Exception) {
            throw RuntimeException("Feil ved oppslag mot EnhetsRegisteret: $e", e)
        }
    }
}