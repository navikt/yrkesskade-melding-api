package no.nav.yrkesskade.ysmeldingapi.models

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonProperty

@JsonIgnoreProperties(ignoreUnknown = true)
data class EnhetsregisterOrganisasjonDto(
    var navn: String? = null,
    var organisasjonsnummer: String? = null,
    @JsonProperty("naeringskode1")
    var naering: Naeringkode? = null
) {
}

data class Naeringkode(
    var beskrivelse: String? = null,
    var kode: String? = null
)
