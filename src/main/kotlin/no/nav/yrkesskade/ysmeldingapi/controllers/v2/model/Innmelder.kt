package no.nav.yrkesskade.ysmeldingapi.controllers.v2.model

import io.swagger.v3.oas.annotations.media.Schema

@Schema(
    name = "Innmelder",
    description = """Representer innmelder"""
)
class Innmelder(
    @Schema(
        description = "Innmelders norske identitetsnummer",
        example = "012345678910"
    )
    val norskIdentitetsnummer: String,

    @Schema(
        description = "Melder skade/sykdom på vegne av en organisasjon",
        example = "123456789"
    )
    val paaVegneAv: String,

    @Schema(
        description = "Rolle innmelder har i forhold til paaVegneAv feltet",
        example = "virksomhetsrepresentat"
    )
    val innmelderrolle: String,

    @Schema(
        description = "En liste av altinn rolle IDer innmelder har",
        example = "[98765, 59647]"
    )
    val altinnRolleIDer: Array<String>)