package no.nav.yrkesskade.ysmeldingapi.skjema

class MilitaerTilsattskjema (
    private val skjemaContext: SkjemaContext,
    private val delegertSkjema: Innmeldingsskjema
) : Innmeldingsskjema by delegertSkjema {

    override fun valider() {
        delegertSkjema.valider()
        val skademelding = skjemaContext.skademelding
        val rolletype = SkjemaUtils.rolletype(skademelding)

        checkNotNull(skademelding.skadelidt.dekningsforhold.underOrdreOmManoever, { "skadelidt.dekningsforhold.underOrdreOmManoever er påkrevd "})

        checkNotNull(skademelding.hendelsesfakta.hvorSkjeddeUlykken, { "skademelding.hendelsesfakta.hvorSkjeddeUlykken er påkrevd "})
        val kodelisteOgVerdi = mutableListOf(
            Pair("hvorSkjeddeUlykken", skademelding.hendelsesfakta.hvorSkjeddeUlykken.orEmpty()),
        )

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