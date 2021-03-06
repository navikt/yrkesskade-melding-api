package no.nav.yrkesskade.ysmeldingapi.security.maskinporten

import com.nimbusds.jose.JWSAlgorithm
import com.nimbusds.jose.JWSHeader
import com.nimbusds.jwt.JWTClaimsSet
import com.nimbusds.jwt.SignedJWT
import no.nav.yrkesskade.ysmeldingapi.utils.getLogger
import org.springframework.beans.factory.InitializingBean
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.boot.web.client.RestTemplateBuilder
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
@ConditionalOnProperty(
    value = arrayOf("maskinporten.client.enabled"),
    havingValue = "true",
    matchIfMissing = true
)
class MaskinportenClientImpl(
    @Value("\${api.client.altinn.url}") val altinnUrl: String,
    val config: MaskinportenConfig,
    restTemplateBuilder: RestTemplateBuilder
): MaskinportenClient, InitializingBean {
    private val logger = getLogger(javaClass)
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

        if (false) { // TODO YSMOD-72 - bruk notProd flagget
            logger.info("maskinporten token: ${tokenResponse.accessToken}")
        }

        return TokenResponseWrapper(
            requestedAt = requestedAt,
            tokenResponse = tokenResponse,
        )
    }
}

@Component
@ConditionalOnProperty(
    value = arrayOf("mock.enabled"),
    havingValue = "true",
    matchIfMissing = false
)
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