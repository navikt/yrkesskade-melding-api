package no.nav.yrkesskade.ysmeldingapi.security.maskinporten

import com.nimbusds.jose.JWSAlgorithm
import com.nimbusds.jose.JWSHeader
import com.nimbusds.jwt.JWTClaimsSet
import com.nimbusds.jwt.SignedJWT
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.InitializingBean
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.web.client.RestTemplateBuilder
import org.springframework.context.annotation.Profile
import org.springframework.http.HttpMethod
import org.springframework.http.MediaType
import org.springframework.http.RequestEntity
import org.springframework.stereotype.Component
import org.springframework.util.LinkedMultiValueMap
import java.time.Duration
import java.time.Instant
import java.util.*

interface MaskinportenClient {
    fun hentAccessToken(): TokenResponseWrapper
}

@Component
@Profile("!local", "!test", "!integration")
class MaskinportenClientImpl(
    @Value("\${api.client.altinn.fallbackUrl}") val altinnUrl: String,
    val config: MaskinportenConfig,
    restTemplateBuilder: RestTemplateBuilder,
): MaskinportenClient, InitializingBean {
    private val logger = LoggerFactory.getLogger(javaClass)
    private val restTemplate = restTemplateBuilder.build()
    private lateinit var wellKnownResponse: WellKnownResponse

    override fun afterPropertiesSet() {
        wellKnownResponse = restTemplate.getForObject(config.wellKnownUrl, WellKnownResponse::class.java)!!
    }

    private fun createClientAssertion(): String {
        val now = Instant.now()
        val expire = now + Duration.ofSeconds(120)

        val claimsSet: JWTClaimsSet = JWTClaimsSet.Builder()
            .audience(wellKnownResponse.issuer)
            .issuer(config.clientId)
            .issueTime(Date.from(now))
            .expirationTime(Date.from(expire))
            .notBeforeTime(Date.from(now))
            .claim("scope", "altinn:serviceowner/reportees altinn:serviceowner/rolesandrights")
            .claim("resource", altinnUrl)
            .jwtID(UUID.randomUUID().toString())
            .build()

        val signedJWT = SignedJWT(
            JWSHeader.Builder(JWSAlgorithm.RS256)
                .keyID(config.privateJwkRsa.keyID)
                .build(),
            claimsSet
        )
        signedJWT.sign(config.jwsSigner)
        return signedJWT.serialize()
    }

    override fun hentAccessToken(): TokenResponseWrapper {
        logger.info("henter ny accesstoken")
        val requestedAt = Instant.now()

        val tokenResponse = restTemplate.exchange(
            RequestEntity
                .method(HttpMethod.POST, wellKnownResponse.tokenEndpoint)
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .body(
                    LinkedMultiValueMap(
                        mapOf(
                            "grant_type" to listOf("urn:ietf:params:oauth:grant-type:jwt-bearer"),
                            "assertion" to listOf(createClientAssertion())
                        )
                    )
                ),
            TokenResponse::class.java
        ).body!!

        logger.info("Fetched new access token. Expires in {} seconds.", tokenResponse.expiresInSeconds)

        return TokenResponseWrapper(
            requestedAt = requestedAt,
            tokenResponse = tokenResponse,
        )
    }
}

@Component
@Profile("local", "test", "integration")
class MaskinportenClientStub: MaskinportenClient {
    override fun hentAccessToken(): TokenResponseWrapper {
        return TokenResponseWrapper(
            requestedAt = Instant.now(),
            tokenResponse = TokenResponse(
                accessToken = "",
                tokenType = "",
                expiresInSeconds = Duration.ofHours(1).toSeconds(),
                scope = "",
            )
        )
    }
}