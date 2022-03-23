package no.nav.yrkesskade.ysmeldingapi.api

import no.nav.yrkesskade.model.SkademeldingBeriketData
import no.nav.yrkesskade.model.SkademeldingMetadata
import no.nav.yrkesskade.model.Spraak
import no.nav.yrkesskade.model.Systemkilde
import no.nav.yrkesskade.skademelding.api.SkademeldingApiDelegate
import no.nav.yrkesskade.skademelding.model.Skademelding
import no.nav.yrkesskade.ysmeldingapi.config.CorrelationInterceptor
import no.nav.yrkesskade.ysmeldingapi.config.FeatureToggleService
import no.nav.yrkesskade.ysmeldingapi.config.FeatureToggles
import no.nav.yrkesskade.ysmeldingapi.services.BrukerinfoService
import no.nav.yrkesskade.ysmeldingapi.services.SkademeldingService
import no.nav.yrkesskade.ysmeldingapi.utils.AutentisertBruker
import org.slf4j.MDC
import org.springframework.http.ResponseEntity
import org.springframework.stereotype.Component
import org.springframework.web.servlet.support.ServletUriComponentsBuilder
import java.time.Instant
import javax.servlet.http.HttpServletRequest
import javax.ws.rs.ForbiddenException

@Component
class SkademeldingApiDelegateImpl(
    private val skademeldingService: SkademeldingService,
    private val autentisertBruker: AutentisertBruker,
    private val httpServletRequest: HttpServletRequest,
    private val brukerinfoService: BrukerinfoService,
    private val featureToggleService: FeatureToggleService
) : SkademeldingApiDelegate {

    override fun sendSkademelding(skademelding: Skademelding): ResponseEntity<Unit> {
        // sjekk at autentisert bruker har tilgang til å poste skademelding
        val organisasjonsnummer = skademelding.innmelder?.paaVegneAv
        val roller = brukerinfoService.hentRollerForFodselsnummerOgOrganisasjon(
            autentisertBruker.fodselsnummer,
            organisasjonsnummer
        )
        val harTilgang = roller.any {
            val rolledefinisjonId = it.rolledefinisjonId
            RolleMedSkjemaTilgang.values().any { it.altinnRolledefinisjonId == rolledefinisjonId }
        }

        if (!harTilgang && !featureToggleService.isEnabled(FeatureToggles.ER_IKKE_PROD.toggleId, false)) {
            // brukeren har ikke tilgang til skjema innsending og vi er i produksjon
            throw ForbiddenException("Bruker har ikke tilgang til å sende skademelding for organisasjon $organisasjonsnummer")
        }

        val skademeldingMetadata = SkademeldingMetadata(
            tidspunktMottatt = Instant.now(),
            kilde = httpServletRequest.getHeader("x-nav-ys-kilde") ?: "ukjent",
            spraak = Spraak.NB,
            navCallId = MDC.get(CorrelationInterceptor.CORRELATION_ID_LOG_VAR_NAME)
        )
        val skademeldingBeriketData = SkademeldingBeriketData(
            innmeldersOrganisasjonsnavn = brukerinfoService.hentOrganisasjonForBruker(
                skademelding.innmelder!!.paaVegneAv
            )?.navn.orEmpty() to Systemkilde.ENHETSREGISTERET
        )
        val lagretSkademeldingDto =
            skademeldingService.lagreSkademelding(skademelding, skademeldingMetadata, skademeldingBeriketData)
        val location = ServletUriComponentsBuilder
            .fromCurrentRequest()
            .path("/{id}")
            .buildAndExpand(lagretSkademeldingDto.id)
            .toUri()

        return ResponseEntity.created(location).build()
    }
}

/*
    Rolledefinisjons IDer fra Altinn som gir tilgang til arbeidsgiver skjema

    3               Lønn og personalmedarbeider
    152             Innehaver
    157             Komplementar
    191             Bestyrende Leder
    173             Deltaker med delt ansvar
    174             Deltaker med fullt ansvar
    5607,5608,5609  Regnskapsfører (dekker alle)
    193             Norsk representant for utenlandsk enhet
    131             Helse-, sosial- og velferdstjenester
    195             Daglig leder / admdir
    196             Bostyrer
    2               Konkursbo skrivetilgang
    8               Begrenset signeringsrettighet
    12              Energi, Miljø og klima
    10              Samferdsel
    130             Signerer av samordnet registermelding
    25000           Taushetsbelagt post
 */
enum class RolleMedSkjemaTilgang(val altinnRolledefinisjonId: Int) {
    LONN_PERSONAL(3),
    INNEHAVER(152),
    KOMPLEMENTAR(157),
    BESTYRENDE_LEDER(191),
    DELTAKER_DELT_ANSVAR(173),
    DELTAKER_FULLT_ANSVAR(174),
    REGNSKAP_LONN(5607),
    REGNSKAP_MED_SIGNERING(5608),
    REGNSKAP_UTEN_SIGNERING(5609),
    NORSK_REPRESENTANT_UTL_ENHET(193),
    HELSE_SOSIAL_VELFERDTJENESTER(131),
    DAGLIG_LEDER_ADM_DIR(195),
    BOSTYRER(196),
    KONKURSBO_SKRIVE(2),
    BEGRENSET_SIGNERINGRETTIGHET(8),
    ENERGI_MILJO_KLIMA(12),
    SAMFERDSEL(10),
    SIGNERER_SAMORDVNET_REGISTERMELDING(130),
    TAUSHETSBELAGT_POST(25000)
}