package no.nav.yrkesskade.ysmeldingapi.mockserver

import com.github.tomakehurst.wiremock.WireMockServer
import com.github.tomakehurst.wiremock.client.MappingBuilder
import com.github.tomakehurst.wiremock.client.WireMock.*
import com.github.tomakehurst.wiremock.core.WireMockConfiguration
import com.github.tomakehurst.wiremock.extension.responsetemplating.ResponseTemplateTransformer
import com.github.tomakehurst.wiremock.matching.UrlPattern
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.stereotype.Component
import java.nio.charset.StandardCharsets
import org.apache.commons.io.IOUtils
import java.net.URL

const val SERVICE_EDITION = "1"
const val SERVICE_CODE = "4936"
const val FNR_MED_SKJEMATILGANG = "01065500791"
const val FNR_MED_ORGANISASJONER = "12345678910"
const val ALTINN_PROXY_PATH = "/altinn/ekstern/altinn/api/serviceowner/reportees*"

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
class MockServer @Autowired constructor(
    @Value("\${mock.port}") private val port: Int,
    @Value("\${api.client.enhetsregister.url}") private val eregUrl: String
) {

    init {
        val config = WireMockConfiguration().apply {
            port(port)
            extensions(ResponseTemplateTransformer(true))
        }

        WireMockServer(config).apply {
            setup()
        }
    }

    private fun WireMockServer.setup() {

        // Altinn
        stubForGet(urlPathMatching(ALTINN_PROXY_PATH)) {
            withQueryParam("subject", equalTo(FNR_MED_ORGANISASJONER))
            willReturnJson(hentStringFraFil("organisasjoner.json"))

        }

        stubForGet(urlPathMatching("$ALTINN_PROXY_PATH.*")) {
            withQueryParam("subject", equalTo(FNR_MED_SKJEMATILGANG))
            withQueryParam("serviceCode", equalTo(SERVICE_CODE))
            withQueryParam("serviceEdition", equalTo(SERVICE_EDITION))
            willReturnJson(hentStringFraFil("rettigheterTilSkjema.json"))
        }

        stubForGet(urlPathEqualTo(ALTINN_PROXY_PATH)) {
            withQueryParam("subject", notMatching("$FNR_MED_ORGANISASJONER|$FNR_MED_SKJEMATILGANG"))
            willReturn(
                aResponse()
                    .withStatusMessage("Invalid socialSecurityNumber")
                    .withStatus(400)
                    .withHeader("Content-Type", "application/octet-stream")
            )
        }

        // Enhetsregisteret
        stubForAny(urlPathMatching("${URL("$eregUrl/910720120").path}.*")) {
            willReturnJson(hentStringFraFil("enhetsregisteret.json"))
        }

        stubForAny(urlPathMatching("${URL("$eregUrl/910720120").path}.*")) {
            willReturnJson(hentStringFraFil("enhetsregisteret.json"))
        }


        start()
    }

    private fun hentStringFraFil(filnavn: String): String {
        return IOUtils.toString(
            MockServer::class.java.classLoader.getResourceAsStream("mock/$filnavn"),
            StandardCharsets.UTF_8
        )
    }
}