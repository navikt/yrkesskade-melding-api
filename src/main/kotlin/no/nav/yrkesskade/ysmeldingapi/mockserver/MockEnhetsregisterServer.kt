package no.nav.yrkesskade.ysmeldingapi.mockserver

import com.github.tomakehurst.wiremock.WireMockServer
import com.github.tomakehurst.wiremock.client.WireMock
import com.github.tomakehurst.wiremock.core.WireMockConfiguration
import com.github.tomakehurst.wiremock.extension.responsetemplating.ResponseTemplateTransformer
import org.apache.commons.io.IOUtils
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Profile
import org.springframework.core.io.support.PathMatchingResourcePatternResolver
import org.springframework.stereotype.Component
import java.lang.invoke.MethodHandles
import java.nio.charset.StandardCharsets

@Component
@Profile("local")
class MockEnhetsregisterServer(@Value("\${mock.enhetsregister.port}") private val port: Int) {
    private val ENHETSREGISTER_PATH = "/enhetsregisteret/api/"
    private val log = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass())

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
        val resources = resolver.getResources("classpath:*mock/enhetsregister/*.json")
        resources.forEach {
            val filnavn = it.filename!!
            if (filnavn.indexOf('.') < 0) {
                // har ikke . i filnavn
                return@forEach
            }
            val orgnummer = filnavn.substring(0, filnavn.indexOf('.'))
            log.info("Setter opp stub for enhetsregister for organisasjon ${orgnummer} til -> mock/enhetsregister/${filnavn}")
            stubForAny(WireMock.urlPathMatching("$ENHETSREGISTER_PATH([enheter|underenheter]*)/${orgnummer}.*")) {
                willReturnJson(hentStringFraFil(filnavn))
            }
        }

    }

    private fun hentStringFraFil(filnavn: String): String {
        return IOUtils.toString(
            MockEnhetsregisterServer::class.java.classLoader.getResourceAsStream("mock/enhetsregister/$filnavn"),
            StandardCharsets.UTF_8
        )
    }
}