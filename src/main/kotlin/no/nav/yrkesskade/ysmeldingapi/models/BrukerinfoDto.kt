package no.nav.yrkesskade.ysmeldingapi.models

data class BrukerinfoDto(val fnr: String, val navn: String, val organisasjoner: List<OrganisasjonDto>)