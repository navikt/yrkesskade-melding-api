package no.nav.yrkesskade.ysmeldingapi.test.docker

import org.testcontainers.containers.KafkaContainer
import org.testcontainers.utility.DockerImageName


class KafkaDockerContainer private constructor() : KafkaContainer(DockerImageName.parse(IMAGE_NAME)) {
    companion object {
        const val IMAGE_NAME = "confluentinc/cp-kafka:7.0.1"
        val container: KafkaDockerContainer by lazy {
            KafkaDockerContainer().apply {
                withReuse(true)
                withEnv("KAFKA_TRANSACTION_STATE_LOG_REPLICATION_FACTOR", "1")
                withEnv("KAFKA_TRANSACTION_STATE_LOG_MIN_ISR", "1")
                start()
                waitUntilContainerStarted()
            }
        }
    }
}