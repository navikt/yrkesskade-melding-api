package no.nav.yrkesskade.ysmeldingapi.mockserver

import com.github.tomakehurst.wiremock.WireMockServer
import com.github.tomakehurst.wiremock.client.MappingBuilder
import com.github.tomakehurst.wiremock.client.WireMock.*
import com.github.tomakehurst.wiremock.core.WireMockConfiguration
import com.github.tomakehurst.wiremock.extension.responsetemplating.ResponseTemplateTransformer
import com.github.tomakehurst.wiremock.matching.UrlPattern
import org.apache.commons.io.IOUtils
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.context.annotation.Profile
import org.springframework.stereotype.Component
import java.nio.charset.StandardCharsets

const val SERVICE_EDITION = "1"
const val SERVICE_CODE = "4936"
const val FNR_UTEN_ORGANISASJONER = "01234567891"
const val FNR_MED_ORGANISASJONER = "12345678910"
const val FNR_MED_ORGANISJON_UTEN_ORGNUMMER = "23456789101"
const val ENHETSREGISTER_PATH = "/enhetsregisteret/api/enheter"
const val UNDERENHETSREGISTER_PATH = "/enhetsregisteret/api/underenheter"
const val ALTINN_ROLLER_PATH = "/altinn/api/serviceowner/authorization/roles"
const val ALTINN_RETTIGHETER_PATH = "/altinn/api/serviceowner/authorization/rights"
const val ALTINN_REPORTEE_PATH = "/altinn/api/serviceowner/reportees"

fun WireMockServer.stubForGet(urlPattern: UrlPattern, builder: MappingBuilder.() -> Unit) {
    stubFor(get(urlPattern).apply(builder))
}

fun WireMockServer.stubForAny(urlPattern: UrlPattern, builder: MappingBuilder.() -> Unit) {
    stubFor(any(urlPattern).apply(builder))
}

fun MappingBuilder.willReturnJson(body: String) {
    willReturn(
        aResponse().apply {
            withHeader("Content-Type", "application/json")
            withBody(body)
        }
    )
}

@Component
@ConditionalOnProperty(
    value = arrayOf("mock.enabled"),
    havingValue = "true",
    matchIfMissing = false
)
@Profile("local")
class MockServer(@Value("\${mock.port}") private val port: Int) : AbstractMockSever(port) {

    init {
        start()
    }

}

open class AbstractMockSever (private val port: Int?){

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

        // Altinn
        stubForGet(urlPathMatching("$ALTINN_ROLLER_PATH.*")) {
            withHeader("ApiKey", equalTo("test"))
            withHeader("authorization", containing("Bearer"))
            withQueryParam("subject", equalTo(FNR_MED_ORGANISASJONER))
            willReturnJson(hentStringFraFil("roller.json"))
        }

        stubForGet(urlPathMatching("$ALTINN_RETTIGHETER_PATH.*")) {
            withHeader("ApiKey", equalTo("test"))
            willReturnJson(hentStringFraFil("rettigheter.json"))
        }

        val altinnReporteeStubs : Array<String> =
            arrayOf(FNR_MED_ORGANISASJONER, FNR_UTEN_ORGANISASJONER, FNR_MED_ORGANISJON_UTEN_ORGNUMMER)

        altinnReporteeStubs.forEach {
            stubForGet(urlPathMatching("$ALTINN_REPORTEE_PATH.*")) {
                withQueryParam("subject", equalTo(it))
                withHeader("authorization", containing("Bearer"))
                withHeader("ApiKey", equalTo("test"))
                willReturnJson(hentStringFraFil("altinn_reportee_${it}.json"))
            }
        }

        // Enhetsregisteret
        stubForAny(urlPathMatching("$UNDERENHETSREGISTER_PATH/910460048.*")) {
            willReturnJson(hentStringFraFil("underenhetregisteret.json"))
        }

        stubForAny(urlPathMatching("$ENHETSREGISTER_PATH/910521551.*")) {
            willReturnJson(hentStringFraFil("enhetsregisteret.json"))
        }

        stubForAny(urlPathMatching("$ENHETSREGISTER_PATH/910460048.*")) {
            willReturnJson(hentStringFraFil("enhetsregisteret.json"))
        }

        stubForAny(urlPathMatching("$ENHETSREGISTER_PATH/910460048.*")) {
            willReturnJson(hentStringFraFil("enhetsregisteret.json"))
        }
    }

    private fun hentStringFraFil(filnavn: String): String {
        return IOUtils.toString(
            MockServer::class.java.classLoader.getResourceAsStream("mock/$filnavn"),
            StandardCharsets.UTF_8
        )
    }
}