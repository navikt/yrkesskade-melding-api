package no.nav.yrkesskade.ysmeldingapi.utils

import no.nav.security.token.support.test.spring.TokenGeneratorConfiguration
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Import
import org.springframework.context.annotation.Profile

/**
 * Setter opp en lokal Tokengenerator i applikasjonen
 *
 * @see <a href="https://github.com/navikt/token-support/blob/97481f89ed56b882943e463bca01baad73dc37ae/token-validation-test-support/README.md">token-validation-test-support</a>
 */
@Configuration
@Import(TokenGeneratorConfiguration::class)
@ConditionalOnProperty(
    value = arrayOf("mock.enabled"),
    havingValue = "true",
    matchIfMissing = false
)
class LocalJWTConfig