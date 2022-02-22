package no.nav.yrkesskade.ysmeldingapi.models

import no.nav.yrkesskade.skademelding.model.Skademelding

data class SkademeldingInnsendtHendelse(
    val metadata: SkademeldingMetadata,
    val skademelding: Skademelding
    )
