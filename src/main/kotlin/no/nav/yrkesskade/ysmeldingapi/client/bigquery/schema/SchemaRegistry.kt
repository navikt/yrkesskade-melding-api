package no.nav.yrkesskade.ysmeldingapi.client.bigquery.schema

import com.fasterxml.jackson.databind.JsonNode
import com.google.cloud.bigquery.*

data class SchemaId(val name: String, val version: Int) {

    fun toTableId(datasetId: DatasetId): TableId = TableId.of(
        datasetId.project,
        datasetId.dataset,
        listOf(name, version).joinToString(SEPARATOR),
    )

    companion object {
        private const val SEPARATOR = "_v"

        fun of(value: String) = value.split(SEPARATOR).let {
            SchemaId(it.first(), it.last().toInt())
        }
    }
}

interface SchemaDefinition {
    val schemaId: SchemaId

    fun entry() = schemaId to this

    fun define(): Schema

    fun transform(payload: JsonNode): InsertAllRequest.RowToInsert

    fun toTableInfo(datasetId: DatasetId): TableInfo {
        val tableDefinition = StandardTableDefinition.of(define())
        return TableInfo.of(schemaId.toTableId(datasetId), tableDefinition)
    }
}

val schemaRegistry: Map<SchemaId, SchemaDefinition> = mapOf(
    skademelding_v1.entry()
)
