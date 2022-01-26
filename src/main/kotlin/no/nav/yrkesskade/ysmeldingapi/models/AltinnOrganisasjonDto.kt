package no.nav.yrkesskade.ysmeldingapi.models

import no.nav.arbeidsgiver.altinnrettigheter.proxy.klient.model.AltinnReportee

data class AltinnOrganisasjonDto(
    var navn: String? = null,
    var parentOrganisasjonsnummer: String? = null,
    var organisasjonsnummer: String? = null,
    var organisasjonsform: String? = null,
    var status: String? = null,
    var type: String? = null
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