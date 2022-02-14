package no.nav.yrkesskade.ysmeldingapi.utils

import com.nimbusds.jwt.JWTClaimsSet
import no.nav.security.token.support.core.context.TokenValidationContext
import no.nav.security.token.support.core.context.TokenValidationContextHolder
import no.nav.security.token.support.core.jwt.JwtTokenClaims
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.Mockito
import org.mockito.Mockito.`when`
import org.mockito.Mockito.mock

internal class AutentisertBrukerTest {

    private val tokenValidationContextHolder = mock(TokenValidationContextHolder::class.java)
    private val tokenValidationContext = mock(TokenValidationContext::class.java)

    @BeforeEach
    fun setup() {
        Mockito.reset(tokenValidationContextHolder, tokenValidationContext)
    }

    @Test
    fun `Autentisert bruker med PID`() {
        val jwtClaimSet = JWTClaimsSet.Builder().apply {
            claim(PID, "pid")
            claim("sub", "subject")
            claim("iss", "issuer")
        }.build()

        `when`(tokenValidationContext.getClaims(Mockito.any())).thenReturn(JwtTokenClaims(jwtClaimSet))
        `when`(tokenValidationContextHolder.tokenValidationContext).thenReturn(tokenValidationContext)

        val autentisertBruker = AutentisertBruker(tokenValidationContextHolder)
        assertThat(autentisertBruker.fodselsnummer).isEqualTo("pid")
    }

    @Test
    fun `Autentisert bruker uten PID`() {
        val jwtClaimSet = JWTClaimsSet.Builder().apply {
            claim("sub", "subject")
            claim("iss", "issuer")
        }.build()

        `when`(tokenValidationContext.getClaims(Mockito.any())).thenReturn(JwtTokenClaims(jwtClaimSet))
        `when`(tokenValidationContextHolder.tokenValidationContext).thenReturn(tokenValidationContext)

        val autentisertBruker = AutentisertBruker(tokenValidationContextHolder)
        assertThat(autentisertBruker.fodselsnummer).isEqualTo("subject")
    }
}