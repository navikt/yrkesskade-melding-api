package no.nav.yrkesskade.ysmeldingapi.models

data class OrganisasjonDto(
    val organisasjonsnummer: String? = null,
    val navn: String? = null,
    val naeringskode: String? = null,
    val organisasjonsform: String? = null,
    val status: String? = null,
    val type: String? = null
)