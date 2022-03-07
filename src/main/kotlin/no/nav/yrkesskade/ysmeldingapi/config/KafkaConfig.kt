package no.nav.yrkesskade.ysmeldingapi.config

import no.nav.yrkesskade.model.SkademeldingInnsendtHendelse
import org.apache.kafka.clients.producer.ProducerConfig
import org.apache.kafka.common.serialization.StringSerializer
import org.springframework.boot.autoconfigure.kafka.KafkaProperties
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.kafka.core.DefaultKafkaProducerFactory
import org.springframework.kafka.core.KafkaTemplate
import org.springframework.kafka.core.ProducerFactory
import org.springframework.kafka.support.serializer.JsonSerializer


@Configuration
class KafkaConfig(val kafkaProperties: KafkaProperties) {

    @Bean
    fun producerConfigs(): Map<String, Any> {
        val props: MutableMap<String, Any> = HashMap(kafkaProperties.buildProducerProperties())
        props[ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG] = StringSerializer::class.java
        props[ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG] = JsonSerializer::class.java
        return props
    }

    @Bean
    fun skademeldingProducerFactory(): ProducerFactory<String, SkademeldingInnsendtHendelse> {
        return DefaultKafkaProducerFactory(producerConfigs())
    }

    @Bean
    fun skademeldingKafkaTemplate(): KafkaTemplate<String, SkademeldingInnsendtHendelse> {
        return KafkaTemplate(skademeldingProducerFactory())
    }
}