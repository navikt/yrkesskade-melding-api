package no.nav.yrkesskade.ysmeldingapi.services

import no.nav.yrkesskade.ysmeldingapi.client.altinn.AltinnClient
import no.nav.yrkesskade.ysmeldingapi.client.enhetsregister.EnhetsregisterClient
import no.nav.yrkesskade.ysmeldingapi.models.AdresseDto
import no.nav.yrkesskade.ysmeldingapi.models.EnhetsregisterOrganisasjonDto
import no.nav.yrkesskade.ysmeldingapi.models.OrganisasjonDto
import org.springframework.stereotype.Service

@Service
class BrukerinfoService(
    private val altinnClient: AltinnClient,
    private val enhetsregisterClient: EnhetsregisterClient
) {

    fun hentOrganisasjonerForFodselsnummer(fnr: String): List<OrganisasjonDto> {
        val altinnOrganisasjoner = altinnClient.hentOrganisasjoner(fnr)
        val enheterForOrganisasjonsnummer: HashMap<String, EnhetsregisterOrganisasjonDto> = HashMap()
        altinnOrganisasjoner.filterNot { it.type == "Person" }.forEach {
            it.organisasjonsnummer?.let { organisasjonsnummer ->
                enheterForOrganisasjonsnummer[organisasjonsnummer] = run {
                    enhetsregisterClient.hentOrganisasjonFraEnhetsregisteret(organisasjonsnummer, false)
                }
            }
        }

        return altinnOrganisasjoner.map{
            OrganisasjonDto(
                organisasjonsform = it.organisasjonsform,
                organisasjonsnummer = it.organisasjonsnummer,
                type = it.type,
                navn = it.navn,
                status = it.status,
                naeringskode = enheterForOrganisasjonsnummer.get(it.organisasjonsnummer)?.naering?.kode
            )
        }
    }

    fun hentOrganisasjonForBruker(fodselsnummer: String, organisasjonsnummer: String): OrganisasjonDto? {
        val enhetsregisterOrganisasjon = enhetsregisterClient.hentOrganisasjonFraEnhetsregisteret(organisasjonsnummer, false)

        return OrganisasjonDto(
            organisasjonsnummer = enhetsregisterOrganisasjon.organisasjonsnummer,
            naeringskode = enhetsregisterOrganisasjon.naering?.kode,
            postadresse = enhetsregisterOrganisasjon.postadresse?.let { AdresseDto.fraEnhetsregisterAdresse(it) },
            forretningsadresse = enhetsregisterOrganisasjon.forretningsadresse?.let { AdresseDto.fraEnhetsregisterAdresse(it) },
        )
    }
}