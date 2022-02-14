package no.nav.yrkesskade.ysmeldingapi.models

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonProperty


@JsonIgnoreProperties(ignoreUnknown = true)
data class AltinnRollerDto(@JsonProperty("roles") val roller: List<AltinnRolleDto>)

@JsonIgnoreProperties(ignoreUnknown = true)
data class AltinnRollerResponse(@JsonProperty("_embedded") val melding: AltinnRollerDto)
