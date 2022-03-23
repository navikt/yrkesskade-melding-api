package no.nav.yrkesskade.ysmeldingapi.metric

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import no.nav.yrkesskade.model.SkademeldingInnsendtHendelse
import no.nav.yrkesskade.ysmeldingapi.client.bigquery.BigQueryClient
import no.nav.yrkesskade.ysmeldingapi.client.bigquery.schema.skademelding_v1
import no.nav.yrkesskade.ysmeldingapi.client.enhetsregister.EnhetsregisterClient
import no.nav.yrkesskade.ysmeldingapi.utils.getLogger
import org.springframework.stereotype.Service

@Service
class MetricService(
    private val metrikkClient: BigQueryClient,
    private val enhetsregisterClient: EnhetsregisterClient
) {
    private val objectMapper: ObjectMapper = jacksonObjectMapper().registerModule(JavaTimeModule())
    private val log = getLogger(MetricService::class.java)

    fun insertMetrikk(record: SkademeldingInnsendtHendelse) {
        var enhet = enhetsregisterClient.hentEnhetFraEnhetsregisteret(record.skademelding.innmelder!!.paaVegneAv, false)
        if (enhet == null) {
            enhet = enhetsregisterClient.hentUnderenhetFraEnhetsregisteret(record.skademelding.innmelder!!.paaVegneAv, false)

            if (enhet == null) {
                log.error("Kunne ikke finne enhet i enhetsregisteret for ${record.skademelding.innmelder!!.paaVegneAv}")
                return
            }
        }

        val skademeldingMetrikkPayload = SkademeldingMetrikkPayload(
            kilde = record.metadata.kilde,
            tidspunktMottatt = record.metadata.tidspunktMottatt,
            spraak = record.metadata.spraak.toString(),
            callId = record.metadata.navCallId,
            naeringskode = enhet.naering?.kode.orEmpty(),
            antallAnsatte = enhet.antallAnsatte ?: -1
        )

        metrikkClient.insert(
            skademelding_v1,
            skademelding_v1.transform(objectMapper.valueToTree(skademeldingMetrikkPayload))
        )
    }
}