package no.nav.yrkesskade.ysmeldingapi.skjema

class Foerstegangstjenesteskjema(
    private val skjemaContext: SkjemaContext,
    private val delegertSkjema: Innmeldingsskjema
) : Innmeldingsskjema by delegertSkjema {

    override fun valider() {
        delegertSkjema.valider()
        val rolletype = SkjemaUtils.rolletype(skjemaContext.skademelding)
        val skademelding = skjemaContext.skademelding
        checkNotNull(skademelding.skadelidt.dekningsforhold.tjenesteperiode, { "skadelidt.dekningsforhold.tjenesteperiode er påkrevd" })

        val tjenesteperiode = skademelding.skadelidt.dekningsforhold.tjenesteperiode!!
        checkNotNull(tjenesteperiode.fra, { "skadelidt.dekningsforhold.tjenesteperiode.fra er påkrevd" })
        checkNotNull(tjenesteperiode.til, { "skadelidt.dekningsforhold.tjenesteperiode.til er påkrevd" })
        check(
            tjenesteperiode.fra!!.isBefore(tjenesteperiode.til!!) || tjenesteperiode.fra!!.isEqual(tjenesteperiode.til!!),
            { "fra dato må være før eller sammme som til dato" })
    }
}