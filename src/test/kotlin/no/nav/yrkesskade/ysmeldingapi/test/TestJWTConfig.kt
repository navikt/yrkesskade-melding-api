package no.nav.yrkesskade.ysmeldingapi.test

import no.nav.security.token.support.test.spring.TokenGeneratorConfiguration
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Import

/**
 * Setter opp en lokal Tokengenerator i applikasjonen
 *
 * @see <a href="https://github.com/navikt/token-support/blob/97481f89ed56b882943e463bca01baad73dc37ae/token-validation-test-support/README.md">token-validation-test-support</a>
 */
@Configuration
@Import(TokenGeneratorConfiguration::class)
class TestJWTConfig