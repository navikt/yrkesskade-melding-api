package no.nav.yrkesskade.ysmeldingapi.models

import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import no.nav.yrkesskade.ysmeldingapi.domain.SkademeldingEntity
import java.time.Instant

data class SkademeldingDto(val id: Int? = null, val skademelding: JsonNode, val kilde: String, val mottattTidspunkt: Instant) {
    fun toSkademelding(): SkademeldingEntity {
        return SkademeldingEntity(
            id = id,
            skademelding = jacksonObjectMapper().registerModule(JavaTimeModule()).writeValueAsString(skademelding),
            kilde = kilde,
            mottattTidspunkt = mottattTidspunkt
        )
    }
}