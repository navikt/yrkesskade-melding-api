package no.nav.yrkesskade.ysmeldingapi.models

data class OrganisasjonDto(
    val organisasjonsnummer: String? = null,
    val navn: String? = null,
    val naeringskode: String? = null,
    val organisasjonsform: String? = null,
    val status: String? = null,
    val type: String? = null,
    val postadresse: AdresseDto? = null,
    val forretningsadresse: AdresseDto? = null,
    val antallAnsatte: Int? = 0
)

data class AdresseDto(
    val landkode: String,
    val land: String,
    val postnummer: String,
    val poststed: String,
    val adresser: List<String>
) {
    companion object {
        fun fraEnhetsregisterAdresse(adresse: Adresse): AdresseDto {
            return AdresseDto(
                land = adresse.land,
                landkode = adresse.landkode,
                postnummer = adresse.postnummer,
                poststed = adresse.poststed,
                adresser = adresse.adresse
            )
        }
    }
}