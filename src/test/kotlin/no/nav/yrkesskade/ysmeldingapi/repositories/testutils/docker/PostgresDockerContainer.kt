package no.nav.yrkesskade.ysmeldingapi.repositories.testutils.docker

import org.testcontainers.containers.PostgreSQLContainer

class PostgresDockerContainer private constructor() : PostgreSQLContainer<PostgresDockerContainer>(IMAGE_NAME) {
    companion object {
        const val IMAGE_NAME = "postgres:14"
        val container: PostgresDockerContainer by lazy {
            PostgresDockerContainer().apply {
                this.start()
            }
        }
    }
}