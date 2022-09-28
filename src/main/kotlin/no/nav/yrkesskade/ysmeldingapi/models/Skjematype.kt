package no.nav.yrkesskade.ysmeldingapi.models

enum class Skjematype(
    val rolletype: String) {
    ARBEIDSTAKER("arbeidstaker"),
    LAERLING("laerling"),
    ELEV_ELLER_STUDENT("elevEllerStudent"),
    TILTAKSDELTAKER("tiltaksdeltaker"),
    VERNEPLIKTIG_I_FOERSTEGANGSTJENESTEN("vernepliktigIFoerstegangstjenesten"),
    INNSATT("innsatt"),
    PERSON_SOM_UTFOERER_SAMFUNNSSTRAFF("personSomUtfoererSamfunnsstraff"),
    PERSON_I_VARETEKT("personIVaretekt")
    ;

    companion object {
        private val map = Skjematype.values().associateBy(Skjematype::rolletype)
        fun hentSkjematypeForNavn(navn: String): Skjematype? = map[navn]
    }
}