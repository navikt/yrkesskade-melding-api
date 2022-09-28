package no.nav.yrkesskade.ysmeldingapi.controllers.v2.model

import com.fasterxml.jackson.annotation.JsonSubTypes
import com.fasterxml.jackson.annotation.JsonTypeInfo
import io.swagger.v3.oas.annotations.media.Schema

@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.PROPERTY, property = "rolletype")
@JsonSubTypes(
    JsonSubTypes.Type(value = ArbeidsstedSkademelding::class, name = "arbeidstaker"),
    JsonSubTypes.Type(value = ArbeidsstedSkademelding::class, name = "laerling")
)
open class Skademelding(
    @Schema(
        description = "Rollen for den skadelidte",
        example = "arbeidstaker"
    )
    val rolletype: String,

    @Schema(
        description = "Informasjon om innmelder"
    )
    val innmelder: Innmelder
    )