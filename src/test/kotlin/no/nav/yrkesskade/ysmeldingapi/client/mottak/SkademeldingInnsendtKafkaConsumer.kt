package no.nav.yrkesskade.ysmeldingapi.client.mottak

import org.apache.kafka.clients.consumer.ConsumerRecord
import org.springframework.kafka.annotation.KafkaListener
import org.springframework.stereotype.Component
import java.util.concurrent.CountDownLatch

@Component
class SkademeldingInnsendtKafkaConsumer {

    private val latch = CountDownLatch(1)
    private lateinit var payload: String

    @KafkaListener(topics = ["\${kafka.topic.skademelding-innsendt}"])
    fun receive(consumerRecord: ConsumerRecord<*, *>) {
        payload = consumerRecord.toString()
        latch.countDown();
    }

    fun getLatch() = latch

    fun getPayload() = payload
}