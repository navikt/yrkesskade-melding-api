package no.nav.yrkesskade.ysmeldingapi.utils

import no.nav.yrkesskade.ysmeldingapi.client.kodeverk.KodeverkClient
import org.springframework.stereotype.Component

@Component
class KodeverkValidator(val kodeverkClient: KodeverkClient) {

    fun sjekkGyldigKodeverkverdiForType(verdi: String, typenavn: String, feilmelding: String) {
        check(kodeverkClient.hentKodeverkForType(typenavn).orEmpty().containsKey(verdi), { feilmelding })
    }

    /**
     * Sjekk på tvers av kodeverdi lister
     */
    fun sjekkGyldigKodeverkverdiForTyper(verdi: String, feilmelding: String, vararg typenavn: String) {
        val gyldig = typenavn.first {
            kodeverkClient.hentKodeverkForType(it).orEmpty().containsKey(verdi)
        }
        checkNotNull(gyldig, { feilmelding })
    }

    fun sjekkGyldigKodeverkverdiForTypeOgKategori(verdi: String, typenavn: String, kategorinavn: String, feilmelding: String) {
        check(kodeverkClient.hentKodeverkForTypeOgKategori(typenavn, kategorinavn).orEmpty().containsKey(verdi), { feilmelding })
    }

}