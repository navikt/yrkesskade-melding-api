package no.nav.yrkesskade.ysmeldingapi.skjema

import no.nav.yrkesskade.ysmeldingapi.models.Skjematype
import java.lang.IllegalStateException

class SkjemaFactory {

    companion object {
        fun hentSkjema(skjemaContext: SkjemaContext): Innmeldingsskjema {
            val grunnskjema = Grunnskjema(skjemaContext)
            return when (skjemaContext.skademelding.skadelidt.dekningsforhold.rolletype) {
                Skjematype.ARBEIDSTAKER.rolletype -> ArbeidstakerEllerLaerlingskjema(skjemaContext, grunnskjema)
                Skjematype.LAERLING.rolletype -> ArbeidstakerEllerLaerlingskjema(skjemaContext, grunnskjema)
                Skjematype.ELEV_ELLER_STUDENT.rolletype -> ElevEllerStudentskjema(skjemaContext, grunnskjema)
                Skjematype.TILTAKSDELTAKER.rolletype -> Tiltaksdeltakerskjema(skjemaContext, grunnskjema)
                Skjematype.VERNEPLIKTIG_I_FOERSTEGANGSTJENESTEN.rolletype -> Foerstegangstjenesteskjema(
                    skjemaContext,
                    grunnskjema
                )

                Skjematype.MILITAER_TILSATT.rolletype -> MilitaerTilsattskjema(
                    skjemaContext,
                    Foerstegangstjenesteskjema(skjemaContext, grunnskjema)
                )

                else -> throw IllegalStateException("${skjemaContext.skademelding.skadelidt.dekningsforhold.rolletype} er ikke en støttet rolletype")
            }
        }
    }
}