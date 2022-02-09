package no.nav.yrkesskade.ysmeldingapi.client.altinn

import no.nav.arbeidsgiver.altinnrettigheter.proxy.klient.AltinnConfig
import no.nav.arbeidsgiver.altinnrettigheter.proxy.klient.AltinnrettigheterProxyKlient
import no.nav.arbeidsgiver.altinnrettigheter.proxy.klient.AltinnrettigheterProxyKlientConfig
import no.nav.arbeidsgiver.altinnrettigheter.proxy.klient.ProxyConfig
import no.nav.arbeidsgiver.altinnrettigheter.proxy.klient.model.AltinnReportee
import no.nav.arbeidsgiver.altinnrettigheter.proxy.klient.model.SelvbetjeningToken
import no.nav.arbeidsgiver.altinnrettigheter.proxy.klient.model.Subject
import no.nav.yrkesskade.ysmeldingapi.models.AltinnOrganisasjonDto
import no.nav.yrkesskade.ysmeldingapi.utils.AutentisertBruker
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component
import java.lang.RuntimeException
import java.util.*

@Component
class AltinnClient(
    private val autentisertBruker: AutentisertBruker,
    @Value("\${spring.application.name}") val applicationName: String,
    @Value("\${api.client.altinn.proxyUrl}") val proxyUrl: String,
    @Value("\${api.client.altinn.fallbackUrl}") val fallbackUrl: String,
    @Value("\${api.client.altinn.apiKey}") val altinnApiKey: String
    ) {

    private val klient: AltinnrettigheterProxyKlient

    init {
        val proxyKlientConfig = AltinnrettigheterProxyKlientConfig(
            ProxyConfig(applicationName, proxyUrl),
            AltinnConfig(fallbackUrl, altinnApiKey, "")
        )
        klient = AltinnrettigheterProxyKlient(proxyKlientConfig)
    }

    fun hentOrganisasjoner(fnr: String): List<AltinnOrganisasjonDto> =
        run {
            klient.hentOrganisasjoner(
                SelvbetjeningToken(autentisertBruker.jwtToken),
                Subject(fnr),
                true
            )
        }

    private fun run(action: () -> List<AltinnReportee>): List<AltinnOrganisasjonDto> =
        try {
            action().map { AltinnOrganisasjonDto.fraAltinnReportee(it) }
        } catch (error: Exception) {
            if (error.message?.contains("403") == true) {
                Collections.emptyList()
            } else {
                throw RuntimeException("Klarte ikke hente organisasjoner fra Altinn")
            }
        }
}