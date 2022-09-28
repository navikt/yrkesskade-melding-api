package no.nav.yrkesskade.ysmeldingapi.skjema

import no.nav.yrkesskade.ysmeldingapi.skjema.SkjemaUtils.Companion.erPeriode
import no.nav.yrkesskade.ysmeldingapi.skjema.SkjemaUtils.Companion.rolletype

class Institusjonsskjema(
    private val skjemaContext: SkjemaContext,
    private val delegertSkjema: Innmeldingsskjema
) : Innmeldingsskjema by delegertSkjema {

    override fun valider() {
        delegertSkjema.valider()
        val skademelding = skjemaContext.skademelding
        val rolletype = rolletype(skademelding)
        val kodelisteOgVerdi = mutableListOf(
            Pair("hvorSkjeddeUlykken", skademelding.hendelsesfakta.hvorSkjeddeUlykken.orEmpty()),
        )

        checkNotNull(skademelding.hendelsesfakta.ulykkessted, { "hendelsesfakta.ulykkested er påkrevd "})
        if (skademelding.hendelsesfakta.ulykkessted!!.adresse != null) {
            skjemaContext.kodeverkValidator.sjekkGyldigKodeverkverdiForType(skademelding.hendelsesfakta.ulykkessted!!.adresse!!.land!!,"landkoderISO2", "${skademelding.hendelsesfakta.ulykkessted!!.adresse!!.land!!} er ikke en gyldig landkode. Sjekk landkoderISO2 for gyldige verdier")
        }

        if (!erPeriode(skademelding)) {
            checkNotNull(skademelding.hendelsesfakta.bakgrunnsaarsak, { "bakgrunnAarsak er påkrevd ved yrkesskade"})
            skademelding.hendelsesfakta.bakgrunnsaarsak!!.forEach {
                kodelisteOgVerdi.add(Pair("bakgrunnForHendelsen", it))
            }
        }

        // rolletype benyttes som kategori navn (elev, arbeidstaker, laerling osv)
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