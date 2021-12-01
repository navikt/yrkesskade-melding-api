package no.nav.yrkesskade.ysmeldingapi.models

import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import no.nav.yrkesskade.ysmeldingapi.domain.Skademelding

data class SkademeldingDto(val id: Int? = null, val skademelding: JsonNode) {
    fun toSkademelding(): Skademelding {
        return Skademelding(
            id = id,
            skademelding = jacksonObjectMapper().writeValueAsString(skademelding),
        )
    }
}