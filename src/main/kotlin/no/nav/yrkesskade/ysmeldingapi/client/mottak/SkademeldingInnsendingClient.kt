package no.nav.yrkesskade.ysmeldingapi.client.mottak

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import no.nav.yrkesskade.ysmeldingapi.models.SkademeldingInnsendtHendelse
import org.springframework.beans.factory.annotation.Value
import org.springframework.kafka.core.KafkaTemplate
import org.springframework.stereotype.Component

@Component
class SkademeldingInnsendingClient(
    @Value("\${kafka.topic.skademelding-innsendt}") private val mottakTopic: String,
    private val kafkaTemplate: KafkaTemplate<String, String>
) {
    fun sendTilMottak(skademelding: SkademeldingInnsendtHendelse): SkademeldingInnsendtHendelse? {
        val skademeldingString = jacksonObjectMapper().writeValueAsString(skademelding)
        val resultat = kafkaTemplate.send(mottakTopic, skademeldingString).get()

        resultat.producerRecord

        return skademelding
    }
}