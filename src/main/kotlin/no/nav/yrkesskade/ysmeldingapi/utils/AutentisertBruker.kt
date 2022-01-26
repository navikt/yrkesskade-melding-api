package no.nav.yrkesskade.ysmeldingapi.utils

import no.nav.security.token.support.core.context.TokenValidationContextHolder
import org.springframework.stereotype.Component

const val LEVEL = "acr=Level4"
const val ISSUER = "selvbetjening"

@Component
class AutentisertBruker(
    val tokenValidationContextHolder: TokenValidationContextHolder
) {
    val fodselsnummer: String
        get() =
            tokenValidationContextHolder
                .tokenValidationContext
                .getClaims(ISSUER)
                .subject

    val jwtToken: String
        get() =
            tokenValidationContextHolder
                .tokenValidationContext
                .getJwtToken(ISSUER)
                .tokenAsString
}