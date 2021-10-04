package no.nav.yrkesskade.ysmeldingapi.models

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import no.nav.yrkesskade.ysmeldingapi.domain.Skademelding
import java.time.Instant

data class SkademeldingDto(val tekst: String, val nummer: Int, val bool: Boolean, val dato: Instant?) {

    fun toSkademelding(): Skademelding {
        return Skademelding(
            id = null,
            json = jacksonObjectMapper().writeValueAsString(this),
            changedBy = "me",
            changedTime = Instant.now()
        )
    }
}

