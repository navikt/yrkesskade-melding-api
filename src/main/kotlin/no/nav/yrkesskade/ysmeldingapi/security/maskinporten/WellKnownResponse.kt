package no.nav.yrkesskade.ysmeldingapi.security.maskinporten

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonProperty

/**
 * e.g.
 * {
 *      "issuer": "https://ver2.maskinporten.no/",
 *      "token_endpoint": "https://ver2.maskinporten.no/token",
 *      "jwks_uri": "https://ver2.maskinporten.no/jwk",
 *      "token_endpoint_auth_methods_supported": [
 *          "private_key_jwt"
 *      ],
 *      "grant_types_supported": [
 *          "urn:ietf:params:oauth:grant-type:jwt-bearer"
 *      ]
 * }
 */
@JsonIgnoreProperties(ignoreUnknown = true)
data class WellKnownResponse(
    @JsonProperty("issuer") val issuer: String,
    @JsonProperty("token_endpoint") val tokenEndpoint: String
)