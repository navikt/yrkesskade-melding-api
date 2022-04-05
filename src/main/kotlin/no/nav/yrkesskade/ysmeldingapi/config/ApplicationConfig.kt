package no.nav.yrkesskade.ysmeldingapi.config

import org.springframework.boot.SpringBootConfiguration
import org.springframework.boot.context.properties.ConfigurationPropertiesScan
import org.springframework.cache.annotation.EnableCaching

@SpringBootConfiguration
@ConfigurationPropertiesScan
@EnableCaching
class ApplicationConfig {
}