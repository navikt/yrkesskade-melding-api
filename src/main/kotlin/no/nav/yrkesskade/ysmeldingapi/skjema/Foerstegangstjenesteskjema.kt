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

        val tjenesteperiodeEllerManoever = skademelding.skadelidt.dekningsforhold.tjenesteperiodeEllerManoever!!
        checkNotNull(tjenesteperiodeEllerManoever.fra, { "skadelidt.dekningsforhold.tjenesteperiodeEllerManoever.fra er påkrevd" })
        checkNotNull(tjenesteperiodeEllerManoever.til, { "skadelidt.dekningsforhold.tjenesteperiodeEllerManoever.til er påkrevd" })
        check(
            tjenesteperiodeEllerManoever.fra!!.isBefore(tjenesteperiodeEllerManoever.til!!) || tjenesteperiodeEllerManoever.fra!!.isEqual(tjenesteperiodeEllerManoever.til!!),
            { "fra dato må være før eller sammme som til dato" })
        val navnPaatjenestegjoerendeavdelingEllerFartoeyEllerStudiested = skademelding.skadelidt.dekningsforhold.navnPaatjenestegjoerendeavdelingEllerFartoeyEllerStudiested
        checkNotNull(navnPaatjenestegjoerendeavdelingEllerFartoeyEllerStudiested, { "skadelidt.dekningsforhold.navnPaatjenestegjoerendeavdelingEllerFartoeyEllerStudiested er påkrevd" })
    }
}