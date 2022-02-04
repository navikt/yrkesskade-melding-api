package no.nav.yrkesskade.ysmeldingapi.models

import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import no.nav.yrkesskade.ysmeldingapi.domain.SkademeldingEntity

data class SkademeldingDto(val id: Int? = null, val skademelding: JsonNode) {
    fun toSkademelding(): SkademeldingEntity {
        return SkademeldingEntity(
            id = id,
            skademelding = jacksonObjectMapper().writeValueAsString(skademelding),
        )
    }
}