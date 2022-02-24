package no.nav.yrkesskade.ysmeldingapi.services

import no.nav.yrkesskade.ysmeldingapi.client.altinn.AltinnClient
import no.nav.yrkesskade.ysmeldingapi.client.enhetsregister.EnhetsregisterClient
import no.nav.yrkesskade.ysmeldingapi.models.AdresseDto
import no.nav.yrkesskade.ysmeldingapi.models.AltinnRettighetResponse
import no.nav.yrkesskade.ysmeldingapi.models.EnhetsregisterOrganisasjonDto
import no.nav.yrkesskade.ysmeldingapi.models.OrganisasjonDto
import org.springframework.kafka.core.KafkaTemplate
import org.springframework.stereotype.Service
import javax.ws.rs.BadRequestException

@Service
class BrukerinfoService(
    private val altinnClient: AltinnClient,
    private val enhetsregisterClient: EnhetsregisterClient
) {

    fun hentOrganisasjonerForFodselsnummer(fnr: String): List<OrganisasjonDto> {
        val altinnOrganisasjoner = altinnClient.hentOrganisasjoner(fnr)
        val enheterForOrganisasjonsnummer: HashMap<String, EnhetsregisterOrganisasjonDto> = HashMap()
        altinnOrganisasjoner.forEach {
            if (it.organisasjonsnummer == null) {
                return@forEach;
            }

            if (it.parentOrganisasjonsnummer != null) {
                enheterForOrganisasjonsnummer[it.parentOrganisasjonsnummer] =
                    enhetsregisterClient.hentUnderenhetFraEnhetsregisteret(it.organisasjonsnummer, false)
            } else {
                enheterForOrganisasjonsnummer[it.organisasjonsnummer] =
                    enhetsregisterClient.hentEnhetFraEnhetsregisteret(it.organisasjonsnummer, false)
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
        val enhetsregisterOrganisasjon = enhetsregisterClient.hentEnhetFraEnhetsregisteret(organisasjonsnummer, false)

        return OrganisasjonDto(
            organisasjonsnummer = enhetsregisterOrganisasjon.organisasjonsnummer,
            naeringskode = enhetsregisterOrganisasjon.naering?.kode,
            postadresse = enhetsregisterOrganisasjon.postadresse?.let { AdresseDto.fraEnhetsregisterAdresse(it) },
            forretningsadresse = enhetsregisterOrganisasjon.forretningsadresse?.let { AdresseDto.fraEnhetsregisterAdresse(it) },
        )
    }

    fun hentSubjectForFodselsnummerOgOrganisasjon(fodselsnummer: String, organisasjon: OrganisasjonDto?): AltinnRettighetResponse? {
        // sjekk om organisasjon er satt
        if (organisasjon == null) {
            return null
        }

        // organisasjon skal alltid har organisasjonsnummer
        if (organisasjon.organisasjonsnummer == null) {
            throw BadRequestException("Organisasjonsnummer kan ikke være tom")
        }

        return altinnClient.hentRettigheter(fodselsnummer, organisasjon.organisasjonsnummer)
    }
}