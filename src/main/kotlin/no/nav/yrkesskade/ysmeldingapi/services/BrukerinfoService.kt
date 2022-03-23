package no.nav.yrkesskade.ysmeldingapi.services

import no.nav.yrkesskade.ysmeldingapi.client.altinn.AltinnClient
import no.nav.yrkesskade.ysmeldingapi.client.enhetsregister.EnhetsregisterClient
import no.nav.yrkesskade.ysmeldingapi.models.*
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
                    enhetsregisterClient.hentUnderenhetFraEnhetsregisteret(it.organisasjonsnummer)
            } else {
                enheterForOrganisasjonsnummer[it.organisasjonsnummer] =
                    enhetsregisterClient.hentEnhetFraEnhetsregisteret(it.organisasjonsnummer)
            }
        }

        return altinnOrganisasjoner.map{
            OrganisasjonDto(
                organisasjonsform = enheterForOrganisasjonsnummer.get(it.organisasjonsnummer)?.organisasjonsform?.kode,
                organisasjonsnummer = it.organisasjonsnummer,
                type = it.type,
                navn = it.navn,
                status = it.status,
                naeringskode = enheterForOrganisasjonsnummer.get(it.organisasjonsnummer)?.naering?.kode,
                antallAnsatte = enheterForOrganisasjonsnummer.get(it.organisasjonsnummer)?.antallAnsatte
            )
        }
    }

    fun hentOrganisasjonForBruker(organisasjonsnummer: String): OrganisasjonDto? {
        val enhetsregisterOrganisasjon = enhetsregisterClient.hentEnhetEllerUnderenhetFraEnhetsregisteret(organisasjonsnummer)

        return OrganisasjonDto(
            organisasjonsnummer = enhetsregisterOrganisasjon.organisasjonsnummer,
            naeringskode = enhetsregisterOrganisasjon.naering?.kode,
            postadresse = enhetsregisterOrganisasjon.postadresse?.let { AdresseDto.fraEnhetsregisterAdresse(it) },
            forretningsadresse = enhetsregisterOrganisasjon.forretningsadresse?.let { AdresseDto.fraEnhetsregisterAdresse(it) },
            beliggenhetsadresse = enhetsregisterOrganisasjon.beliggenhetsadresse?.let { AdresseDto.fraEnhetsregisterAdresse(it) },
            organisasjonsform = enhetsregisterOrganisasjon.organisasjonsform?.kode,
            antallAnsatte = enhetsregisterOrganisasjon.antallAnsatte
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

    fun hentRollerForFodselsnummerOgOrganisasjon(fodselsnummer: String, organisasjonsnummer: String?): List<AltinnRolleDto> {
        if (organisasjonsnummer == null) {
            return emptyList()
        }

        return altinnClient.hentRoller(fodselsnummer, organisasjonsnummer).roller
    }
}