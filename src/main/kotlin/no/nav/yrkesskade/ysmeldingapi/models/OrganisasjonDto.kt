package no.nav.yrkesskade.ysmeldingapi.models

data class OrganisasjonDto(
    var organisasjonsnummer: String? = null,
    var navn: String? = null,
    var naeringskode: String? = null,
    var organisasjonsform: String? = null,
    var status: String? = null,
    var type: String? = null
) {
}