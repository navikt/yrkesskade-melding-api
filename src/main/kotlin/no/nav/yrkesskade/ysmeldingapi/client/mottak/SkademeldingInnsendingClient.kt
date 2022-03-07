package no.nav.yrkesskade.ysmeldingapi.client.mottak

import no.nav.yrkesskade.model.SkademeldingInnsendtHendelse
import org.springframework.beans.factory.annotation.Value
import org.springframework.kafka.core.KafkaTemplate
import org.springframework.kafka.support.SendResult
import org.springframework.stereotype.Component
import org.springframework.util.concurrent.ListenableFuture

@Component
class SkademeldingInnsendingClient(
    @Value("\${kafka.topic.skademelding-innsendt}") private val mottakTopic: String,
    private val skademeldingKafkaTemplate: KafkaTemplate<String, SkademeldingInnsendtHendelse>
) {

    fun sendTilMottak(skademelding: SkademeldingInnsendtHendelse): SkademeldingInnsendtHendelse {
        val future: ListenableFuture<SendResult<String, SkademeldingInnsendtHendelse>> = skademeldingKafkaTemplate.send(mottakTopic, skademelding)
        val resultat = future.get()
        return resultat.producerRecord.value()
    }
}