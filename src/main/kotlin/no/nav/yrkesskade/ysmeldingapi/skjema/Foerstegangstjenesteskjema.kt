package no.nav.yrkesskade.ysmeldingapi.skjema

class Foerstegangstjenesteskjema(
    private val skjemaContext: SkjemaContext,
    private val delegertSkjema: Innmeldingsskjema
) : Innmeldingsskjema by delegertSkjema {

    override fun valider() {
        delegertSkjema.valider()
        val rolletype = SkjemaUtils.rolletype(skjemaContext.skademelding)
        val skademelding = skjemaContext.skademelding
        checkNotNull(skademelding.skadelidt.dekningsforhold.tjenesteperiodeEllerManoever, { "skadelidt.dekningsforhold.tjenesteperiodeEllerManoever er påkrevd" })

        val tjenesteperiode = skademelding.skadelidt.dekningsforhold.tjenesteperiodeEllerManoever!!
        checkNotNull(tjenesteperiode.fra, { "skadelidt.dekningsforhold.tjenesteperiodeEllerManoever.fra er påkrevd" })
        checkNotNull(tjenesteperiode.til, { "skadelidt.dekningsforhold.tjenesteperiodeEllerManoever.til er påkrevd" })
        check(
            tjenesteperiode.fra!!.isBefore(tjenesteperiode.til!!) || tjenesteperiode.fra!!.isEqual(tjenesteperiode.til!!),
            { "fra dato må være før eller sammme som til dato" })
        val tjenestegjoerendeAvdeling = skademelding.skadelidt.dekningsforhold.tjenestegjoerendeAvdelingNavnPaaFartoey
        checkNotNull(tjenestegjoerendeAvdeling, { "skadelidt.dekningsforhold.tjenestegjoerendeAvdelingNavnPaaFartoey er påkrevd" })
    }
}