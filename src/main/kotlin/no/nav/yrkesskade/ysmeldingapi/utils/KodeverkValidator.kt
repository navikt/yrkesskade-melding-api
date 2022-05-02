package no.nav.yrkesskade.ysmeldingapi.utils

import no.nav.yrkesskade.ysmeldingapi.client.kodeverk.KodeverkClient
import org.springframework.stereotype.Component

@Component
class KodeverkValidator(val kodeverkClient: KodeverkClient) {

    fun sjekkGyldigKodeverkverdiForType(verdi: String, typenavn: String, feilmelding: String) {
        check(kodeverkClient.hentKodeverkForType(typenavn).orEmpty().containsKey(verdi), { feilmelding })
    }

    fun sjekkGyldigKodeverkverdiForTypeOgKategori(verdi: String, typenavn: String, kategorinavn: String, feilmelding: String) {
        check(kodeverkClient.hentKodeverkForTypeOgKategori(typenavn, kategorinavn).orEmpty().containsKey(verdi), { feilmelding })
    }

}