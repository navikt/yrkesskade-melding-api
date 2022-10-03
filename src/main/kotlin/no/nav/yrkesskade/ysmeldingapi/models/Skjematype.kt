package no.nav.yrkesskade.ysmeldingapi.models

enum class Skjematype(
    val rolletype: String) {
    ARBEIDSTAKER("arbeidstaker"),
    LAERLING("laerling"),
    ELEV_ELLER_STUDENT("elevEllerStudent"),
    TILTAKSDELTAKER("tiltaksdeltaker"),
    VERNEPLIKTIG_I_FOERSTEGANGSTJENESTEN("vernepliktigIFoerstegangstjenesten"),
    MILITAER_TILSATT("militaerTilsatt"),
    MILITAER_LAERLING("militaerLaerling"),
    MILITAER_ELEV("militaerElev"),
    VERNEPLIKTIG_I_REPETISJONSTJENESTE("vernepliktigIRepetisjonstjeneste"),
    MILITAER_FRIVILLIG_TJENESTEGJOERENDE("frivilligTjenestegjoerendeIForsvaret")
    ;

    companion object {
        private val map = Skjematype.values().associateBy(Skjematype::rolletype)
        fun hentSkjematypeForNavn(navn: String): Skjematype? = map[navn]
    }
}