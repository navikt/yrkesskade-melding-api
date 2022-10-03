package no.nav.yrkesskade.ysmeldingapi.skjema

import java.time.LocalDate

class MilitaerElevskjema(
    private val skjemaContext: SkjemaContext,
    private val delegertSkjema: Innmeldingsskjema
) : Innmeldingsskjema by delegertSkjema {

    override fun valider() {
        delegertSkjema.valider()
        val skademelding = skjemaContext.skademelding
        val rolletype = SkjemaUtils.rolletype(skademelding)

        checkNotNull(skademelding.skadelidt.dekningsforhold.underOrdreOmManoever, { "skadelidt.dekningsforhold.underOrdreOmManoever er påkrevd "})

        checkNotNull(
            skademelding.skadelidt.dekningsforhold.utdanningStart,
            { "skademelding.skadelidt.dekningsforhold.utdanningStart er påkrevd"}
        )

        check(skademelding.skadelidt.dekningsforhold.utdanningStart!!.isBefore(LocalDate.now()),
            { "skademelding.skadelidt.dekningsforhold.utdanningStart kan ikke være i framtiden "}
        )
    }
}