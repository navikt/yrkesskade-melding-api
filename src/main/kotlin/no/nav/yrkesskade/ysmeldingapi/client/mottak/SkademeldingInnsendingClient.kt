package no.nav.yrkesskade.ysmeldingapi.client.mottak

import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import no.nav.yrkesskade.model.SkademeldingInnsendtHendelse
import org.springframework.beans.factory.annotation.Value
import org.springframework.kafka.core.KafkaTemplate
import org.springframework.stereotype.Component

@Component
class SkademeldingInnsendingClient(
    @Value("\${kafka.topic.skademelding-innsendt}") private val mottakTopic: String,
    private val kafkaTemplate: KafkaTemplate<String, String>
) {
    val objectMapper = jacksonObjectMapper().registerModule(JavaTimeModule())

    fun sendTilMottak(skademelding: SkademeldingInnsendtHendelse): SkademeldingInnsendtHendelse {
        val skademeldingString = objectMapper.writeValueAsString(skademelding)
        val resultat = kafkaTemplate.send(mottakTopic, skademeldingString).get()

        return objectMapper.readValue(resultat.producerRecord.value(), SkademeldingInnsendtHendelse::class.java)
    }
}