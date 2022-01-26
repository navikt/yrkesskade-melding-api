package no.nav.yrkesskade.ysmeldingapi.test

import no.nav.yrkesskade.ysmeldingapi.repositories.testutils.docker.PostgresDockerContainer
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase
import org.springframework.context.ApplicationContextInitializer
import org.springframework.context.ConfigurableApplicationContext
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.ContextConfiguration
import org.springframework.test.context.support.TestPropertySourceUtils

@ActiveProfiles("integration")
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@ContextConfiguration(initializers = [AbstractIT.DockerPostgresDataSourceInitializer::class])
abstract class AbstractIT {

    init {
        PostgresDockerContainer.container
    }

    class DockerPostgresDataSourceInitializer: ApplicationContextInitializer<ConfigurableApplicationContext> {
        override fun initialize(applicationContext: ConfigurableApplicationContext) {
            TestPropertySourceUtils.addInlinedPropertiesToEnvironment(
                applicationContext,
                "spring.datasource.url=" + PostgresDockerContainer.container.jdbcUrl,
                "spring.datasource.username=" + PostgresDockerContainer.container.username,
                "spring.datasource.password=" + PostgresDockerContainer.container.password
            );
        }
    }
}