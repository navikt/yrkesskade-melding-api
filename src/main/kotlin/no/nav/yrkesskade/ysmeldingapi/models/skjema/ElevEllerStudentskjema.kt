package no.nav.yrkesskade.ysmeldingapi.models.skjema

import no.nav.yrkesskade.ysmeldingapi.models.skjema.SkjemaUtils.Companion.rolletype

class ElevEllerStudentskjema(
    private val skjemaContext: SkjemaContext,
    private val delegertSkjema: Innmeldingsskjema
    ) : Innmeldingsskjema by delegertSkjema {

    override fun valider() {
        delegertSkjema.valider()
        val rolletype = rolletype(skjemaContext.skademelding)
        val kodelisteOgVerdi = mutableListOf(
            Pair("hvorSkjeddeUlykken", skjemaContext.skademelding.hendelsesfakta.hvorSkjeddeUlykken),
        )

        if (skjemaContext.skademelding.hendelsesfakta.ulykkessted.adresse != null) {
            skjemaContext.kodeverkValidator.sjekkGyldigKodeverkverdiForType(skjemaContext.skademelding.hendelsesfakta.ulykkessted.adresse!!.land!!,"landkoderISO2", "${skjemaContext.skademelding.hendelsesfakta.ulykkessted.adresse!!.land!!} er ikke en gyldig landkode. Sjekk landkoderISO2 for gyldige verdier")
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