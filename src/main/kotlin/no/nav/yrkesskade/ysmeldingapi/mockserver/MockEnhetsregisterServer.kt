package no.nav.yrkesskade.ysmeldingapi.mockserver

import com.github.tomakehurst.wiremock.WireMockServer
import com.github.tomakehurst.wiremock.client.WireMock
import com.github.tomakehurst.wiremock.core.WireMockConfiguration
import com.github.tomakehurst.wiremock.extension.responsetemplating.ResponseTemplateTransformer
import no.nav.yrkesskade.ysmeldingapi.utils.getLogger
import org.apache.commons.io.IOUtils
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.core.io.support.PathMatchingResourcePatternResolver
import org.springframework.stereotype.Component
import java.lang.invoke.MethodHandles
import java.nio.charset.StandardCharsets

@Component
@ConditionalOnProperty(
    value = arrayOf("mock.enhetsregister.port"),
    havingValue = "10094",
    matchIfMissing = false
)
class MockEnhetsregisterServer(@Value("\${mock.enhetsregister.port}") private val port: Int) {
    private val ENHETSREGISTER_PATH = "/enhetsregisteret/api/"
    private val log = getLogger(MethodHandles.lookup().lookupClass())

    private val mockServer: WireMockServer

    init {
        val config = WireMockConfiguration().apply {
            if (port != null) {
                port(port)
            } else {
                dynamicPort()
            }
            extensions(ResponseTemplateTransformer(true))
        }

        mockServer = WireMockServer(config).apply {
            setup()
        }

        start()
    }

    fun start() {
        if (!mockServer.isRunning) {
            mockServer.start()
        }
    }

    fun stop() {
        if (mockServer.isRunning) {
            mockServer.stop()
        }
    }

    fun port(): Int {
        return mockServer.port()
    }

    private fun WireMockServer.setup() {

        val resolver = PathMatchingResourcePatternResolver(MockEnhetsregisterServer::class.java.classLoader)
        val resources = resolver.getResources("classpath:*mock/enhetsregister/enhet/*.json")
        resources.forEach {
            val filnavn = it.filename!!
            if (filnavn.indexOf('.') < 0) {
                // har ikke . i filnavn
                return@forEach
            }
            val orgnummer = filnavn.substring(0, filnavn.indexOf('.'))
            log.info("Wiremock stub ${ENHETSREGISTER_PATH}enheter/${orgnummer} til -> mock/enhetsregister/enhet/${filnavn}")
            stubForAny(WireMock.urlPathMatching("${ENHETSREGISTER_PATH}enheter/${orgnummer}.*")) {
                willReturnJson(hentStringFraFil(filnavn, "enhet"))
            }
        }

        val underenhetResources = resolver.getResources("classpath:*mock/enhetsregister/underenhet/*.json")
        underenhetResources.forEach {
            val filnavn = it.filename!!
            if (filnavn.indexOf('.') < 0) {
                // har ikke . i filnavn
                return@forEach
            }
            val orgnummer = filnavn.substring(0, filnavn.indexOf('.'))
            log.info("Wiremock stub ${ENHETSREGISTER_PATH}underenheter/${orgnummer} til -> mock/enhetsregister/underenhet/${filnavn}")
            stubForAny(WireMock.urlPathMatching("${ENHETSREGISTER_PATH}underenheter/${orgnummer}.*")) {
                willReturnJson(hentStringFraFil(filnavn, "underenhet"))
            }
        }

    }

    private fun hentStringFraFil(filnavn: String, enhet: String): String {
        return IOUtils.toString(
            MockEnhetsregisterServer::class.java.classLoader.getResourceAsStream("mock/enhetsregister/$enhet/$filnavn"),
            StandardCharsets.UTF_8
        )
    }
}