package no.nav.yrkesskade.ysmeldingapi.client.bigquery.schema

import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.treeToValue
import com.google.cloud.bigquery.InsertAllRequest
import com.google.cloud.bigquery.Schema
import no.nav.yrkesskade.ysmeldingapi.metric.SkademeldingMetrikkPayload

val skademelding_v1 = object : SchemaDefinition {

    override val schemaId: SchemaId = SchemaId(name = "skademelding_api", version = 1)
    val objectMapper = jacksonObjectMapper().registerModule(JavaTimeModule())

    override fun define(): Schema = schema {
        string("kilde") {
            required()
            description("Systemet som sendte skademeldingen")
        }
        timestamp("tidspunktMottatt") {
            required()
            description("Tidspunkt da skademeldingen ble mottatt")
        }
        string("spraak") {
            required()
            description("Skademeldingens språk")
        }
        string("callId") {
            required()
            description("Unik ID for innmeldingens systemtransaksjon")
        }
        string("naeringskode") {
            required()
            description("Næringskode for innmelders organisasjon")
        }
        numeric("antallAnsatte") {
            required()
            description("Antall ansatte i innmelders organisasjon")
        }
    }

    override fun transform(payload: JsonNode): InsertAllRequest.RowToInsert {
        val skademeldingPayload = objectMapper.treeToValue<SkademeldingMetrikkPayload>(payload)
        return InsertAllRequest.RowToInsert.of(
            mapOf(
                "kilde" to skademeldingPayload.kilde,
                "tidspunktMottatt" to skademeldingPayload.tidspunktMottatt.toString(),
                "spraak" to skademeldingPayload.spraak,
                "callId" to skademeldingPayload.callId,
                "naeringskode" to skademeldingPayload.naeringskode,
                "antallAnsatte" to skademeldingPayload.antallAnsatte,
            )
        )
    }
}
