package no.nav.yrkesskade.ysmeldingapi.client.enhetsregister

import no.nav.yrkesskade.ysmeldingapi.models.EnhetsregisterOrganisasjonDto
import org.springframework.beans.factory.annotation.Value
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Component
import javax.ws.rs.client.Client
import javax.ws.rs.client.ClientBuilder
import javax.ws.rs.core.MediaType
import javax.ws.rs.core.Response

private const val UNDERENHET = "underenheter"
private const val ENHET = "enheter"
private const val DEV = "organisasjoner"

@Component
class EnhetsregisterClient(
    @Value("\${api.client.enhetsregister.url}") val enhetsregisterUrl: String
) {
    private val client: Client = ClientBuilder.newClient()

    fun hentEnhetFraEnhetsregisteret(
        orgnr: String,
        inkluderHistorikk: Boolean
    ): EnhetsregisterOrganisasjonDto = hentEnhet(orgnr, inkluderHistorikk, ENHET)

    fun hentUnderenhetFraEnhetsregisteret(
        orgnr: String,
        inkluderHistorikk: Boolean
    ): EnhetsregisterOrganisasjonDto = hentEnhet(orgnr, inkluderHistorikk, UNDERENHET)

    /**
     * Henter en enhet fra enten enheter eller underenheter
     */
    private fun hentEnhet(orgnr: String, inkluderHistorikk: Boolean, enhettype: String): EnhetsregisterOrganisasjonDto {
        if (enhettype != ENHET && enhettype != UNDERENHET) {
            throw RuntimeException("$enhettype er ikke en gyldig enhetstype. Forventet $ENHET eller $UNDERENHET")
        }

        var path = enhettype;
        if (enhetsregisterUrl.contains("/ereg/api/v1")) {
            // dersom vi er i dev, går må vi bruke "/organisasjoner som path
            path = DEV
        }

        return try {
            val response: Response = client.target(enhetsregisterUrl)
                .path(path)
                .path(orgnr)
                .request(MediaType.APPLICATION_JSON)
                .get()

            if (response.status == HttpStatus.NOT_FOUND.value()) {
                // dersom det ikke finnes en enhet  i registeret, returneres en tom organisasjon
                return EnhetsregisterOrganisasjonDto()
            }

            response.readEntity(EnhetsregisterOrganisasjonDto::class.java)
        } catch (e: Exception) {
            throw RuntimeException("Feil ved oppslag mot EnhetsRegisteret: $e", e)
        }
    }
}