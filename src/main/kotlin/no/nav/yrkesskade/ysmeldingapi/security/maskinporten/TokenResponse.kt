package no.nav.yrkesskade.ysmeldingapi.security.maskinporten

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonProperty
import java.time.Duration
import java.time.Instant

@JsonIgnoreProperties(ignoreUnknown = true)
data class TokenResponse(
    @JsonProperty("access_token") val accessToken: String,
    @JsonProperty("token_type") val tokenType: String,
    @JsonProperty("expires_in") val expiresInSeconds: Long,
    @JsonProperty("scope") val scope: String,
) {
    val expiresIn: Duration = Duration.ofSeconds(expiresInSeconds)
}

data class TokenResponseWrapper(
    val requestedAt: Instant,
    val tokenResponse: TokenResponse,
) {
    private val validFor = tokenResponse.expiresIn
    private val expiresAt = requestedAt + validFor

    fun expiresIn(now: Instant = Instant.now()): Duration = Duration.between(now, expiresAt)

    fun percentageRemaining(now: Instant = Instant.now()): Double {
        val timeToExpire = expiresIn(now).seconds.toDouble()
        return if (timeToExpire <= 0)
            0.0
        else
            100.0 * (timeToExpire / validFor.seconds)
    }
}