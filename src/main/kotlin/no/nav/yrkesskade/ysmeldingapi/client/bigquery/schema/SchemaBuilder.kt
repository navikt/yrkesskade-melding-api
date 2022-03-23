package no.nav.yrkesskade.ysmeldingapi.client.bigquery.schema

import com.google.cloud.bigquery.Field
import com.google.cloud.bigquery.FieldList
import com.google.cloud.bigquery.Schema
import com.google.cloud.bigquery.StandardSQLTypeName

class FieldBuilder(private val name: String, private val type: StandardSQLTypeName) {
    private var mode: Field.Mode = Field.Mode.NULLABLE
    private var description: String? = null
    private var subFields: FieldList? = null

    fun nullable() {
        this.mode = Field.Mode.NULLABLE
    }

    fun required() {
        this.mode = Field.Mode.REQUIRED
    }

    fun repeated() {
        this.mode = Field.Mode.REPEATED
    }

    fun description(description: String) {
        this.description = description
    }

    fun subFields(block: SchemaBuilder.() -> Unit) {
        this.subFields = SchemaBuilder().apply(block).fieldList()
    }

    fun build(): Field = Field.newBuilder(name, type, subFields)
        .setMode(mode)
        .setDescription(description)
        .build()
}

class SchemaBuilder {
    private val fields = mutableListOf<Field>()

    fun fieldList(): FieldList = FieldList.of(fields)

    fun build(): Schema = Schema.of(fields)

    private fun field(
        name: String,
        type: StandardSQLTypeName,
        block: FieldBuilder.() -> Unit = {},
    ): Field = FieldBuilder(name, type)
        .apply(block)
        .build()
        .also { fields.add(it) }

    fun datetime(
        name: String,
        block: FieldBuilder.() -> Unit = {},
    ): Field = field(name, StandardSQLTypeName.DATETIME, block)

    fun string(
        name: String,
        block: FieldBuilder.() -> Unit = {},
    ): Field = field(name, StandardSQLTypeName.STRING, block)

    fun numeric(
        name: String,
        block: FieldBuilder.() -> Unit = {},
    ): Field = field(name, StandardSQLTypeName.NUMERIC, block)

    fun boolean(
        name: String,
        block: FieldBuilder.() -> Unit,
    ): Field = field(name, StandardSQLTypeName.BOOL, block)

    fun struct(
        name: String,
        block: FieldBuilder.() -> Unit = {},
    ): Field = field(name, StandardSQLTypeName.STRUCT, block)

    fun timestamp(
        name: String,
        block: FieldBuilder.() -> Unit = {},
    ): Field = field(name, StandardSQLTypeName.TIMESTAMP, block)
}

fun schema(block: SchemaBuilder.() -> Unit): Schema = SchemaBuilder()
    .apply(block)
    .build()