package no.nav.yrkesskade.ysmeldingapi.models

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonProperty

@JsonIgnoreProperties(ignoreUnknown = true)
data class AltinnRettighetResponse(@JsonProperty("Subject") val person: AltinnPerson)

@JsonIgnoreProperties(ignoreUnknown = true)
data class AltinnPerson(@JsonProperty("Name") val navn: String)