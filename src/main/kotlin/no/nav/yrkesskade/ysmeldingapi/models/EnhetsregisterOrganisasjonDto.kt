package no.nav.yrkesskade.ysmeldingapi.models

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonProperty

@JsonIgnoreProperties(ignoreUnknown = true)
data class EnhetsregisterOrganisasjonDto(
    val navn: String? = null,
    val organisasjonsnummer: String? = null,
    @JsonProperty("naeringskode1")
    val naering: Naeringkode? = null,
    val postadresse: Adresse? = null,
    val forretningsadresse: Adresse? = null,
    val beliggenhetsadresse: Adresse? = null
)

data class Naeringkode(
    val beskrivelse: String? = null,
    val kode: String? = null
)

data class Adresse(
    val land: String,
    val landkode: String,
    val postnummer: String,
    val poststed: String,
    val adresse: List<String>,
    val kommune: String, val kommunenummer: String
)