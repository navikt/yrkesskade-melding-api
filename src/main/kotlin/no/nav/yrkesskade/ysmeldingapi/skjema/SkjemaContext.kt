package no.nav.yrkesskade.ysmeldingapi.skjema

import no.nav.yrkesskade.skademelding.model.Skademelding
import no.nav.yrkesskade.ysmeldingapi.client.enhetsregister.EnhetsregisterClient
import no.nav.yrkesskade.ysmeldingapi.config.FeatureToggleService
import no.nav.yrkesskade.ysmeldingapi.utils.KodeverkValidator

data class SkjemaContext(
    val skademelding: Skademelding,
    val kodeverkValidator: KodeverkValidator,
    val enhetsregisterClient: EnhetsregisterClient,
    val featureToggleService: FeatureToggleService
    )