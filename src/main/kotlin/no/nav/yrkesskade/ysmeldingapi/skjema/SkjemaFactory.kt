package no.nav.yrkesskade.ysmeldingapi.skjema

import no.nav.yrkesskade.ysmeldingapi.models.Skjematype
import java.lang.IllegalStateException

class SkjemaFactory {

    companion object {
        fun hentSkjema(skjemaContext: SkjemaContext): Innmeldingsskjema {
            val grunnskjema = Grunnskjema(skjemaContext)
            val foerstegangstjenesteskjema = Foerstegangstjenesteskjema(
                skjemaContext,
                grunnskjema
            )
            val militaerTilsattskjema = MilitaerTilsattskjema(
                skjemaContext,
                foerstegangstjenesteskjema
            )
            return when (skjemaContext.skademelding.skadelidt.dekningsforhold.rolletype) {
                Skjematype.ARBEIDSTAKER.rolletype -> ArbeidstakerEllerLaerlingskjema(skjemaContext, grunnskjema)
                Skjematype.LAERLING.rolletype -> ArbeidstakerEllerLaerlingskjema(skjemaContext, grunnskjema)
                Skjematype.ELEV_ELLER_STUDENT.rolletype -> ElevEllerStudentskjema(skjemaContext, grunnskjema)
                Skjematype.TILTAKSDELTAKER.rolletype -> Tiltaksdeltakerskjema(skjemaContext, grunnskjema)
                Skjematype.VERNEPLIKTIG_I_FOERSTEGANGSTJENESTEN.rolletype -> foerstegangstjenesteskjema
                Skjematype.VERNEPLIKTIG_I_REPETISJONSTJENESTE.rolletype -> foerstegangstjenesteskjema
                Skjematype.MILITAER_TILSATT.rolletype -> militaerTilsattskjema
                Skjematype.MILITAER_LAERLING.rolletype -> MilitaerLaerlingskjema(skjemaContext, militaerTilsattskjema)
                Skjematype.MILITAER_ELEV.rolletype -> MilitaerElevskjema(skjemaContext, foerstegangstjenesteskjema)
                Skjematype.MILITAER_FRIVILLIG_TJENESTEGJOERENDE.rolletype -> foerstegangstjenesteskjema

                else -> throw IllegalStateException("${skjemaContext.skademelding.skadelidt.dekningsforhold.rolletype} er ikke en støttet rolletype")
            }
        }

    }
}