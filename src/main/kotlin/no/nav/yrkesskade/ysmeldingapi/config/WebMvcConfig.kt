package no.nav.yrkesskade.ysmeldingapi.config

import org.springframework.context.annotation.Configuration
import org.springframework.web.servlet.config.annotation.InterceptorRegistry
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer

/**
 * A [WebMvcConfigurer] used for adding custom handler interceptors.
 * TODO forkaster dette viktig Spring-funksjonalitet?
 */
@Configuration
class WebMvcConfig(val correlationInterceptor: CorrelationInterceptor) : WebMvcConfigurer {
    override fun addInterceptors(registry: InterceptorRegistry) {
        registry.addInterceptor(correlationInterceptor)
    }
}