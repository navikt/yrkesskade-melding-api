package no.nav.yrkesskade.ysmeldingapi.client.bigquery.schema

import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import no.nav.yrkesskade.model.Spraak
import no.nav.yrkesskade.ysmeldingapi.metric.SkademeldingMetrikkPayload
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import java.time.Instant
import kotlin.reflect.full.memberProperties

internal class SchemaTest {

    val objectMapper = jacksonObjectMapper().registerModule(JavaTimeModule())

    @Test
    internal fun `payload mappes riktig til en skademelding_v1 row`() {
        val payload = SkademeldingMetrikkPayload(
            kilde = "digital",
            tidspunktMottatt = Instant.now(),
            spraak = Spraak.NB.toString(),
            callId = "callId",
            naeringskode = "64.21",
            antallAnsatte = 10
        )

        val content = skademelding_v1.transform(objectMapper.valueToTree(payload)).content
        assertThat(content["kilde"]).isEqualTo(payload.kilde)
        assertThat(content["tidspunktMottatt"]).isEqualTo(payload.tidspunktMottatt.toString())
        assertThat(content["spraak"]).isEqualTo(payload.spraak)
        assertThat(content["callId"]).isEqualTo(payload.callId)
        assertThat(content["naeringskode"]).isEqualTo(payload.naeringskode)
        assertThat(content["antallAnsatte"]).isEqualTo(payload.antallAnsatte)
    }

    @Test
    internal fun `skademelding_v1 schema skal ha samme antall felter som payload `() {
        assertThat(SkademeldingMetrikkPayload::class.memberProperties.size)
            .isEqualTo(skademelding_v1.define().fields.size)
    }
}