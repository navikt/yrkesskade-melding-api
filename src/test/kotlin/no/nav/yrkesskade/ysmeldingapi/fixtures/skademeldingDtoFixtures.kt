package no.nav.yrkesskade.ysmeldingapi.fixtures

import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import no.nav.yrkesskade.skademelding.model.*
import no.nav.yrkesskade.ysmeldingapi.models.Adresse
import no.nav.yrkesskade.ysmeldingapi.models.SkademeldingDto
import java.nio.file.Files
import java.nio.file.Path
import java.time.Instant
import java.time.ZoneOffset

private val objectMapper = jacksonObjectMapper().registerModule(JavaTimeModule())

fun enkelSkademelding(): SkademeldingDto {
    return SkademeldingDto(
        null,
        objectMapper.valueToTree(fullSkademelding()),
        "test-kilde",
        Instant.now()
    )
}

fun skademelding_ok(rolletype: String, meldingtype: String): String {
    return Files.readString(Path.of("src/test/resources/skademeldinger/$rolletype/${rolletype}_${meldingtype}_ok.json"))
}

fun skademelding_feil(rolletype: String, meldingtype: String): String {
    return Files.readString(Path.of("src/test/resources/skademeldinger/$rolletype/${rolletype}_${meldingtype}_feil.json"))
}

fun skademeldingMedFeilStillingstittelFormat(): String {
    return Files.readString(Path.of("src/test/resources/skademeldinger/feilStillingstittelFormat.json"))
}

fun skademeldingMedPeriodeFraDatoSammeSomTilDato(): String {
    return Files.readString(Path.of("src/test/resources/skademeldinger/fradatoSammeSomTilDato.json"))
}

fun skademeldingMedPeriodeOgSykdomsinformasjon(): String {
    return Files.readString(Path.of("src/test/resources/skademeldinger/yrkessykdom.json"))
}

fun fullSkademelding(): Skademelding {
    return Skademelding(
        innmelder = arbeidsgiverInnmelder(),
        skadelidt = skadelidt(),
        skade = skade(),
        hendelsesfakta = hendelsesfakta()
    )
}

fun arbeidsgiverInnmelder(): Innmelder {
    return Innmelder(
        norskIdentitetsnummer = "10117424370",
        paaVegneAv = "910521551",
        innmelderrolle = "virksomhetsrepresentant",
        altinnrolleIDer = emptyList()
    )
}

fun hendelsesfakta(): Hendelsesfakta {
    return Hendelsesfakta(
        tid = Tid(Tidstype.tidspunkt, tidspunkt = Instant.now().atOffset(ZoneOffset.UTC)),
        naarSkjeddeUlykken = "alternativenePasserIkke",
        hvorSkjeddeUlykken = "arbeidsstedInne",
        aarsakUlykke = listOf("sammenstoetEllerBittEllerSpark"),
        bakgrunnsaarsak = listOf("mangelfulleSikkerhetsrutiner"),
        stedsbeskrivelse = "alternativenePasserIkke",
        ulykkessted = Ulykkessted(sammeSomVirksomhetensAdresse = true, adresse = Ulykkesadresse(adresselinje1 = "test 1", adresselinje2 = "test 2", adresselinje3 = "test 3", land = "NO")),
        utfyllendeBeskrivelse = "Dette var dumt"
    )
}

fun skade(): Skade {
    return Skade(
        skadedeDeler = listOf(SkadetDel("kuldeskade", kroppsdel = "ribbenOgSkulderblad")),
        alvorlighetsgrad = "antattOppsoektLege",
        antattSykefravaer = "alternativenePasserIkke"
    )
}

fun skadelidt(): Skadelidt {
    return Skadelidt(
        norskIdentitetsnummer = "16120101181",
        dekningsforhold = Dekningsforhold(
            organisasjonsnummer = "910441205",
            stillingstittelTilDenSkadelidte = listOf("sivilingeniorerByggOgAnlegg"),
            rolletype = "arbeidstaker",
            navnPaaVirksomheten = "Test"
        )
    )
}