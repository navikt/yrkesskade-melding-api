package no.nav.yrkesskade.ysmeldingapi.models.skjema

import no.nav.yrkesskade.ysmeldingapi.models.skjema.SkjemaUtils.Companion.erPeriode

class Tiltaksdeltakerskjema(
    private val skjemaContext: SkjemaContext,
    private val delegertSkjema: Innmeldingsskjema
) : Innmeldingsskjema by delegertSkjema {

    override fun valider() {
        delegertSkjema.valider()

        val rolletype = SkjemaUtils.rolletype(skjemaContext.skademelding)
        val kodelisteOgVerdi = mutableListOf(
            Pair("hvorSkjeddeUlykken", skjemaContext.skademelding.hendelsesfakta.hvorSkjeddeUlykken),
        )

        if (!erPeriode(skjemaContext.skademelding)) {
            checkNotNull(skjemaContext.skademelding.hendelsesfakta.bakgrunnsaarsak, { "bakgrunnAarsak er påkrevd ved yrkesskade"})
            skjemaContext.skademelding.hendelsesfakta.bakgrunnsaarsak!!.forEach {
                kodelisteOgVerdi.add(Pair("bakgrunnForHendelsen", it))
            }
        }

        kodelisteOgVerdi.forEach {
            skjemaContext.kodeverkValidator.sjekkGyldigKodeverkverdiForTypeOgKategori(
                it.second,
                it.first,
                rolletype,
                "${it.second} er ikke en gyldig ${it.first} verdi. Sjekk kodeliste for gyldige verdier"
            )
        }
    }
}