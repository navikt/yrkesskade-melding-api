package no.nav.yrkesskade.ysmeldingapi.models

data class AltinnOrganisasjonDto(
    val navn: String? = null,
    val parentOrganisasjonsnummer: String? = null,
    val organisasjonsnummer: String? = null,
    val organisasjonsform: String? = null,
    val status: String? = null,
    val type: String? = null
) {
    companion object {
        fun fraAltinnReportee(altinnReportee: AltinnReportee): AltinnOrganisasjonDto {
            return AltinnOrganisasjonDto(
                navn = altinnReportee.navn,
                parentOrganisasjonsnummer = altinnReportee.hovedOrganisasjonsnummer,
                organisasjonsnummer = altinnReportee.organisasjonsnummer,
                status = altinnReportee.status,
                type = altinnReportee.type,
                organisasjonsform = altinnReportee.organisasjonsform
            )
        }
    }
}