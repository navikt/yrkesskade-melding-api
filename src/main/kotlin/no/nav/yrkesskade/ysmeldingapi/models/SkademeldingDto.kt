package no.nav.yrkesskade.ysmeldingapi.models

import java.time.Instant

data class SkademeldingDto(val tekst: String, val nummer: Int, val bool: Boolean, val dato: Instant?)