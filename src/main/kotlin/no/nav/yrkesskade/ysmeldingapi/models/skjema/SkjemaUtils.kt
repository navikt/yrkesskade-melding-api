package no.nav.yrkesskade.ysmeldingapi.models.skjema

import no.nav.yrkesskade.skademelding.model.Skademelding
import no.nav.yrkesskade.skademelding.model.Tidstype

class SkjemaUtils {

    companion object {
        fun erPeriode(skademelding: Skademelding) : Boolean = skademelding.hendelsesfakta.tid.tidstype == Tidstype.periode
        fun rolletype(skademelding: Skademelding) : String = skademelding.skadelidt.dekningsforhold.rolletype
    }
}