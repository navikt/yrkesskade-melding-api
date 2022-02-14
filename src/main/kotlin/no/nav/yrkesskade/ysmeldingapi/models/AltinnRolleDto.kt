package no.nav.yrkesskade.ysmeldingapi.models

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonProperty

@JsonIgnoreProperties(ignoreUnknown = true)
data class AltinnRolleDto(
    @JsonProperty("RoleId")
    val rolleId: String,
    @JsonProperty("RoleType")
    val rolletype: String,
    @JsonProperty("RoleDefinitionId")
    val rolledefinisjonId: Int,
    @JsonProperty("RoleName")
    val rollenavn: String,
    @JsonProperty("RoleDescription")
    val rollebeskrivelse: String
)