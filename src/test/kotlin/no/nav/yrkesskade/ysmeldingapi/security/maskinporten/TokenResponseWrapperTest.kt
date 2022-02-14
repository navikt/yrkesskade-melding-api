package no.nav.yrkesskade.ysmeldingapi.security.maskinporten

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import java.time.Duration
import java.time.Instant

internal class TokenResponseWrapperTest {
    @Test
    fun expiresIn() {
        assertExpiresIn(age = 120, tokenExpire = 120, expectedExpiresIn = 0)
        assertExpiresIn(age = 120, tokenExpire = 60, expectedExpiresIn = -60)
        assertExpiresIn(age = 120, tokenExpire = 180, expectedExpiresIn = 60)
    }

    @Test
    fun percentageRemaining() {
        assertPercentageRemaining(age = 90, tokenExpire = 180, expectedPercentageRemaining = 50.0)
        assertPercentageRemaining(age = 180, tokenExpire = 180, expectedPercentageRemaining = 0.0)
        assertPercentageRemaining(age = 200, tokenExpire = 180, expectedPercentageRemaining = 0.0)
        assertPercentageRemaining(age = 135, tokenExpire = 180, expectedPercentageRemaining = 25.0)
        assertPercentageRemaining(age = 1440, tokenExpire = 3600, expectedPercentageRemaining = 60.0)
    }

    @Suppress("SameParameterValue")
    private fun assertPercentageRemaining(age: Long, tokenExpire: Long, expectedPercentageRemaining: Double) {
        val now = Instant.now()
        val requestedAt = now.minus(Duration.ofSeconds(age))
        val percentageRemaining = TokenResponseWrapper(
            requestedAt = requestedAt,
            TokenResponse(
                expiresInSeconds = tokenExpire,
                accessToken = "",
                tokenType = "",
                scope = "",
            )
        ).percentageRemaining(now)
        assertThat(expectedPercentageRemaining).isEqualTo(percentageRemaining)
    }

    @Suppress("SameParameterValue")
    private fun assertExpiresIn(age: Long, tokenExpire: Long, expectedExpiresIn: Long) {
        assertThat(expiresIn(age = age, expireSeconds = tokenExpire))
            .isEqualTo(Duration.ofSeconds(expectedExpiresIn))
    }

    private fun expiresIn(
        expireSeconds: Long,
        age: Long
    ): Duration {
        val now = Instant.now()
        val requestedAt = now.minus(Duration.ofSeconds(age))
        return TokenResponseWrapper(
            requestedAt = requestedAt,
            TokenResponse(
                expiresInSeconds = expireSeconds,
                accessToken = "",
                tokenType = "",
                scope = "",
            )
        ).expiresIn(now)
    }
}