package no.nav.yrkesskade.ysmeldingapi.client.kodeverk

import no.nav.yrkesskade.kodeverk.model.KodeverdiDto
import no.nav.yrkesskade.kodeverk.model.KodeverdiResponsDto
import no.nav.yrkesskade.ysmeldingapi.config.CorrelationInterceptor
import no.nav.yrkesskade.ysmeldingapi.config.CorrelationInterceptor.Companion.CORRELATION_ID_HEADER_NAME
import no.nav.yrkesskade.ysmeldingapi.utils.getLogger
import org.slf4j.MDC
import org.springframework.beans.factory.annotation.Value
import org.springframework.retry.annotation.Retryable
import org.springframework.stereotype.Component
import javax.ws.rs.client.Client
import javax.ws.rs.client.ClientBuilder
import javax.ws.rs.core.MediaType
import javax.ws.rs.core.Response

@Component
class KodeverkClient(@Value("\${api.client.kodeverk.url}") val kodeverkUrl: String) {

    private val client: Client

    companion object {
        @Suppress("JAVA_CLASS_ON_COMPANION")
        private val log = getLogger(javaClass.enclosingClass)
    }

    init {
        client =  ClientBuilder.newBuilder().build()
    }

    @Retryable
    fun hentKodeverkForType(type: String, spraak: String = "nb"): Map<String, KodeverdiDto>? {
        log.info(
            "Kaller ys-kodeverk - type=$type, spraak=$spraak"
        )

         return kallKodeverkApi(type, null)
    }

    @Retryable
    fun hentKodeverkForTypeOgKategori(type: String, kategori: String, spraak: String = "nb"): Map<String, KodeverdiDto>? {
        log.info(
            "Kaller ys-kodeverk - type=$type, kategori=$kategori, spraak=$spraak"
        )

        return kallKodeverkApi(type, kategori)
    }

    private fun kallKodeverkApi(typenavn: String, kategorinavn: String?): Map<String, KodeverdiDto>? {
        val path = if (kategorinavn != null) "/typer/$typenavn/kategorier/$kategorinavn/kodeverdier" else "/typer/$typenavn/kodeverdier"
        log.info("Kaller på kodeverk: url = $kodeverkUrl, path = $path")

        val respons: Response = client.target(kodeverkUrl)
            .path(path)
            .request(MediaType.APPLICATION_JSON)
            .header(
                CORRELATION_ID_HEADER_NAME, MDC.get(
                    CorrelationInterceptor.CORRELATION_ID_LOG_VAR_NAME
                )
            ).get()

        if (respons.statusInfo.family != Response.Status.Family.SUCCESSFUL) {
            log.error("Kodeverk klient feil:  ${respons.readEntity(String::class.java)}")
            throw Exception("${respons.status} ${respons.readEntity(String::class.java)}")
        }

        return respons.readEntity(KodeverdiResponsDto::class.java).kodeverdierMap
    }
}