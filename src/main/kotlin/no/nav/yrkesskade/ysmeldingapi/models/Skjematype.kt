package no.nav.yrkesskade.ysmeldingapi.models

enum class Skjematype(
    val rolletype: String,
    val harStilling: Boolean = false,
    val harStedsbeskrivelse: Boolean = false,
    val harBakgrunnAarsak: Boolean = true,
    ) {
    ARBEIDSTAKER("arbeidstaker", harStedsbeskrivelse = true, harStilling = true),
    LAERLING("laerling", harStedsbeskrivelse = true, harStilling = true),
    ELEV_ELLER_STUDENT("elevEllerStudent", harBakgrunnAarsak = false),
    TILTAKSDELTAKER("tiltaksdeltaker");

    companion object {
        private val map = Skjematype.values().associateBy(Skjematype::rolletype)
        fun hentSkjematypeForNavn(navn: String): Skjematype? = map[navn]
    }
}