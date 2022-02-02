package no.nav.yrkesskade.ysmeldingapi.models

import no.nav.arbeidsgiver.altinnrettigheter.proxy.klient.model.AltinnReportee

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
                navn = altinnReportee.name,
                parentOrganisasjonsnummer = altinnReportee.parentOrganizationNumber,
                organisasjonsnummer = altinnReportee.organizationNumber,
                status = altinnReportee.status,
                type = altinnReportee.type,
                organisasjonsform = altinnReportee.organizationForm
            )
        }
    }
}