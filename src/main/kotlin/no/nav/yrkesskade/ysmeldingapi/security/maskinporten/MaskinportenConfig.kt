package no.nav.yrkesskade.ysmeldingapi.security.maskinporten

import com.nimbusds.jose.JWSSigner
import com.nimbusds.jose.crypto.RSASSASigner
import com.nimbusds.jose.jwk.RSAKey
import org.springframework.beans.factory.InitializingBean
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.stereotype.Component

/**
 * Plukker opp miljø-variablene som nais injecter i poden.
 * Se: https://doc.nais.io/security/auth/maskinporten/client/#runtime-variables-and-credentials
 */
@Component
@ConditionalOnProperty(
    value = arrayOf("mock.enabled"),
    havingValue = "false",
    matchIfMissing = false
)
@ConfigurationProperties("maskinporten")
class MaskinportenConfig : InitializingBean {
    lateinit var scopes: String
    lateinit var wellKnownUrl: String
    lateinit var clientId: String
    lateinit var clientJwk: String

    lateinit var privateJwkRsa: RSAKey
    lateinit var jwsSigner: JWSSigner

    override fun afterPropertiesSet() {
        privateJwkRsa = RSAKey.parse(clientJwk)
        jwsSigner = RSASSASigner(privateJwkRsa)
    }
}