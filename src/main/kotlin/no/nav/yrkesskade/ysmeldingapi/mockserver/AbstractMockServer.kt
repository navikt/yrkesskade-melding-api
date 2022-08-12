package no.nav.yrkesskade.ysmeldingapi.mockserver

import com.github.tomakehurst.wiremock.WireMockServer
import com.github.tomakehurst.wiremock.client.MappingBuilder
import com.github.tomakehurst.wiremock.client.WireMock.*
import com.github.tomakehurst.wiremock.core.WireMockConfiguration
import com.github.tomakehurst.wiremock.extension.responsetemplating.ResponseTemplateTransformer
import com.github.tomakehurst.wiremock.matching.UrlPattern
import no.nav.yrkesskade.ysmeldingapi.models.Skjematype
import no.nav.yrkesskade.ysmeldingapi.utils.getLogger
import org.apache.commons.io.IOUtils
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.context.annotation.Profile
import org.springframework.stereotype.Component
import java.lang.invoke.MethodHandles
import java.nio.charset.StandardCharsets

const val FNR_UTEN_ORGANISASJONER = "01234567891"
const val FNR_MED_ORGANISASJONER = "12345678910"
const val FNR_MED_ORGANISJON_UTEN_ORGNUMMER = "23456789101"
const val FNR_MED_EXCEPTION = "1"
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

open class AbstractMockSever(private val port: Int?) {

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
        val reportees = arrayOf("01234567891", "12345678910", "23456789101")
        reportees.forEach {
            val mappe = it

            log.info("Wiremock stub ${ALTINN_REPORTEE_PATH} til -> mock/altinn/${mappe}/reportee.json")
            stubForGet(urlPathMatching("$ALTINN_REPORTEE_PATH.*")) {
                withQueryParam("subject", equalTo(mappe))
                withHeader("authorization", containing("Bearer"))
                withHeader("ApiKey", equalTo("test"))
                willReturnJson(hentStringFraFil("altinn/${mappe}/reportee.json"))
            }

            log.info("Wiremock stub ${ALTINN_ROLLER_PATH} til -> mock/altinn/${mappe}/roller.json")
            stubForGet(urlPathMatching("$ALTINN_ROLLER_PATH.*")) {
                withQueryParam("subject", equalTo(mappe))
                withHeader("authorization", containing("Bearer"))
                withHeader("ApiKey", equalTo("test"))
                willReturnJson(hentStringFraFil("altinn/${mappe}/roller.json"))
            }

            log.info("Wiremock stub ${ALTINN_RETTIGHETER_PATH} til -> mock/altinn/${mappe}/rettigheter.json")
            stubForGet(urlPathMatching("$ALTINN_RETTIGHETER_PATH.*")) {
                withQueryParam("subject", equalTo(mappe))
                withHeader("authorization", containing("Bearer"))
                withHeader("ApiKey", equalTo("test"))
                willReturnJson(hentStringFraFil("altinn/${mappe}/rettigheter.json"))
            }
        }

        // Altinn bruker med exception
        stubForGet(urlPathMatching("$ALTINN_REPORTEE_PATH.*")) {
            withQueryParam("subject", equalTo(FNR_MED_EXCEPTION))
            willReturn(
                aResponse().apply {
                    withStatus(400)
                    withBody("Sannsynlig feil fra Altinn")
                }
            )
        }

        // Enhetsregisteret
        stubForAny(urlPathMatching("$UNDERENHETSREGISTER_PATH/910460048.*")) {
            willReturnJson(hentStringFraFil("underenhetregisteret.json"))
        }

        stubForAny(urlPathMatching("$UNDERENHETSREGISTER_PATH/910441205.*")) {
            willReturnJson(hentStringFraFil("underenhetregisteret.json"))
        }

        stubForAny(urlPathMatching("$ENHETSREGISTER_PATH/910521551.*")) {
            willReturnJson(hentStringFraFil("enhetsregisteret.json"))
        }

        val kodeverkMedKategorier = listOf(
            "tidsrom",
            "alvorlighetsgrad",
            "hvorSkjeddeUlykken",
            "aarsakOgBakgrunn",
            "skadetKroppsdel",
            "skadetype",
            "harSkadelidtHattFravaer",
            "bakgrunnForHendelsen",
            "stillingstittel",
            "typeArbeidsplass"
        )
        kodeverkMedKategorier.forEach {
            log.info("Wiremock stub $it til -> mock/kodeverk/$it.json")
            val kategori = it
            Skjematype.values().forEach {
                stubForGet(urlPathMatching("/api/v1/kodeverk/typer/$kategori/kategorier/${it.rolletype}/kodeverdier")) {
                    willReturnJson(hentStringFraFil("kodeverk/$kategori.json"))
                }
            }
        }

        val kodeverkUtenKategorier = listOf<String>("fravaertype", "landkoderISO2", "rolletype", "sykdomstype", "paavirkningsform", "skadetKroppsdel", "skadetype")
        kodeverkUtenKategorier.forEach {
            log.info("Wiremock stub $it til -> mock/kodeverk/$it.json")
            stubForGet(urlPathMatching("/api/v1/kodeverk/typer/$it/kodeverdier")) {
                willReturnJson(hentStringFraFil("kodeverk/$it.json"))
            }
        }

    }

    private fun hentStringFraFil(filnavn: String): String {
        return IOUtils.toString(
            AbstractMockSever::class.java.classLoader.getResourceAsStream("mock/$filnavn"),
            StandardCharsets.UTF_8
        )
    }
}