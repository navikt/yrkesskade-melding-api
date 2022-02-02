package no.nav.yrkesskade.ysmeldingapi.models

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonProperty

@JsonIgnoreProperties(ignoreUnknown = true)
data class EnhetsregisterOrganisasjonDto(
    val navn: String? = null,
    val organisasjonsnummer: String? = null,
    @JsonProperty("naeringskode1")
    val naering: Naeringkode? = null
)

data class Naeringkode(
    val beskrivelse: String? = null,
    val kode: String? = null
)
