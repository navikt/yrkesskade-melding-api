package no.nav.yrkesskade.ysmeldingapi.config

import no.finn.unleash.DefaultUnleash
import no.finn.unleash.UnleashContext
import no.finn.unleash.UnleashContextProvider
import no.finn.unleash.util.UnleashConfig
import no.nav.yrkesskade.featureflag.strategy.ByClusterName
import no.nav.yrkesskade.featureflag.strategy.IsNotProdStrategy
import no.nav.yrkesskade.ysmeldingapi.utils.getLogger
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.boot.context.properties.ConstructorBinding
import org.springframework.context.annotation.Bean
import org.springframework.core.env.Environment
import java.lang.invoke.MethodHandles
import java.net.URI

@ConfigurationProperties("funksjonsbrytere")
@ConstructorBinding
class FeatureToggleConfig(
    private val enabled: Boolean,
    val unleash: Unleash
) {

    @ConstructorBinding
    data class Unleash(
        val uri: URI,
        val cluster: String,
        val applicationName: String
    )

    @Autowired
    lateinit var environment: Environment

    @Bean
    fun featureToggle(): FeatureToggleService =
        if (enabled)
            lagUnleashFeatureToggleService()
        else {
            logger.warn(
                "Funksjonsbryter-funksjonalitet er skrudd AV. " +
                        "Gir standardoppførsel for alle funksjonsbrytere, dvs 'false'"
            )
            lagDummyFeatureToggleService()
        }

    private fun lagUnleashFeatureToggleService(): FeatureToggleService {
        val defaultUnleash = DefaultUnleash(
            UnleashConfig.builder()
                .appName(unleash.applicationName)
                .unleashAPI(unleash.uri)
                .unleashContextProvider(lagUnleashContextProvider())
                .build(),
            ByClusterName(unleash.cluster),
            IsNotProdStrategy(lagUnleashContextProvider().context)
        )

        return object : FeatureToggleService {
            override fun isEnabled(toggleId: String, defaultValue: Boolean): Boolean {
                return defaultUnleash.isEnabled(toggleId, defaultValue)
            }
        }
    }

    private fun lagUnleashContextProvider(): UnleashContextProvider {
        return UnleashContextProvider {
            UnleashContext.builder()
                .appName(unleash.applicationName)
                .environment(environment.activeProfiles.first().orEmpty())
                .build()
        }
    }

    private fun lagDummyFeatureToggleService(): FeatureToggleService {
        return object : FeatureToggleService {
            override fun isEnabled(toggleId: String, defaultValue: Boolean): Boolean {
                if (toggleId == FeatureToggles.ER_IKKE_PROD.toggleId && environment.activeProfiles.first().orEmpty() != "integration") {
                    return true;
                }

                if (unleash.cluster == "lokal") {
                    return false
                }

                return defaultValue
            }
        }
    }

    companion object {
        private val logger = getLogger(MethodHandles.lookup().lookupClass())
    }
}

interface FeatureToggleService {

    fun isEnabled(toggleId: String): Boolean {
        return isEnabled(toggleId, false)
    }

    fun isEnabled(toggleId: String, defaultValue: Boolean = false): Boolean
}

enum class FeatureToggles(val toggleId: String) {
    ER_IKKE_PROD("yrkesskade.er-ikke-prod"),
    DIGITAL_SKJEMA_INNSENDING("yrkesskade.digital-skjema-innsending");
}
