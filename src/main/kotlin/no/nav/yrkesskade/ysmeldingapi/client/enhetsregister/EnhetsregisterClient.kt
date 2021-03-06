package no.nav.yrkesskade.ysmeldingapi.client.enhetsregister

import no.nav.yrkesskade.ysmeldingapi.models.EnhetsregisterOrganisasjonDto
import org.springframework.beans.factory.annotation.Value
import org.springframework.cache.annotation.Cacheable
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Component
import javax.ws.rs.client.Client
import javax.ws.rs.client.ClientBuilder
import javax.ws.rs.core.MediaType
import javax.ws.rs.core.Response

private const val UNDERENHET = "underenheter"
private const val ENHET = "enheter"

@Component
class EnhetsregisterClient(
    @Value("\${api.client.enhetsregister.url}") val enhetsregisterUrl: String
) {
    private val client: Client = ClientBuilder.newClient()

    fun hentEnhetFraEnhetsregisteret(
        orgnr: String
    ): EnhetsregisterOrganisasjonDto = hentEnhet(orgnr, ENHET)

    fun hentUnderenhetFraEnhetsregisteret(
        orgnr: String
    ): EnhetsregisterOrganisasjonDto = hentEnhet(orgnr, UNDERENHET)

    @Cacheable(value = ["enhetsregister"], key = "{#orgnr}")
    fun hentEnhetEllerUnderenhetFraEnhetsregisteret(
        orgnr: String
    ): EnhetsregisterOrganisasjonDto {
        val enhet = hentEnhet(orgnr, ENHET)
        if (enhet != EnhetsregisterOrganisasjonDto()) {
            return enhet
        }
        return hentEnhet(orgnr, UNDERENHET)
    }

    /**
     * Henter en enhet fra enten enheter eller underenheter
     */
    private fun hentEnhet(orgnr: String, enhettype: String): EnhetsregisterOrganisasjonDto {
        if (enhettype != ENHET && enhettype != UNDERENHET) {
            throw RuntimeException("$enhettype er ikke en gyldig enhetstype. Forventet $ENHET eller $UNDERENHET")
        }

        return try {
            val response: Response = client.target(enhetsregisterUrl)
                .path(enhettype)
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