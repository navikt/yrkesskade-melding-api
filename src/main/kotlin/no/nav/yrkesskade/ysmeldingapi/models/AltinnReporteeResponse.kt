package no.nav.yrkesskade.ysmeldingapi.models

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonProperty

@JsonIgnoreProperties(ignoreUnknown = true)
data class AltinnReporteeResponse(@JsonProperty("_embedded") val embedded: AltinnReporteeEmbedded)

@JsonIgnoreProperties(ignoreUnknown = true)
data class AltinnReporteeEmbedded(@JsonProperty("reportees") val reportees: Array<AltinnReportee>)

@JsonIgnoreProperties(ignoreUnknown = true)
data class AltinnReportee(
    @JsonProperty("Name") val navn: String,
    @JsonProperty("Type") val type: String,
    @JsonProperty("OrganizationNumber") val organisasjonsnummer: String?,
    @JsonProperty("ParentOrganizationNumber") val hovedOrganisasjonsnummer: String?,
    @JsonProperty("OrganizationForm") val organisasjonsform: String?,
    @JsonProperty("Status") val status: String?,
    @JsonProperty("SocialSecurityNumber") val fodselsnummer: String?
)