package no.nav.yrkesskade.ysmeldingapi.metric

import java.time.Instant

data class SkademeldingMetrikkPayload(
    val kilde: String,
    val tidspunktMottatt: Instant,
    val spraak: String,
    val callId: String,
    val naeringskode: String,
    val antallAnsatte: Int,
)