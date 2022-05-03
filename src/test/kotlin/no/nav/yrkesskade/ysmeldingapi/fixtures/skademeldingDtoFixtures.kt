package no.nav.yrkesskade.ysmeldingapi.fixtures

import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import no.nav.yrkesskade.skademelding.model.*
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

fun skademeldingMedFeilStillingstittelFormat(): String {
    return Files.readString(Path.of("src/test/resources/skademeldinger/feilStillingstittelFormat.json"))
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
        norskIdentitetsnummer = "3093242309",
        paaVegneAv = "910521551",
        innmelderrolle = "virksomhetsrepresentant",
        altinnrolleIDer = null
    )
}

fun hendelsesfakta(): Hendelsesfakta {
    return Hendelsesfakta(
        tid = Tid(Tidstype.tidspunkt, tidspunkt = Instant.now().atOffset(ZoneOffset.UTC)),
        naarSkjeddeUlykken = "alternativenePasserIkke",
        hvorSkjeddeUlykken = "arbeidsstedInne",
        aarsakUlykkeTabellAogE = listOf("sammenstoetEllerBittEllerSpark"),
        bakgrunnsaarsakTabellBogG = listOf("mangelfulleSikkerhetsrutiner"),
        stedsbeskrivelseTabellF = "alternativenePasserIkke",
        ulykkessted = Ulykkessted(sammeSomVirksomhetensAdresse = true, adresse = Adresse(adresselinje1 = "test 1", adresselinje2 = "test 2", adresselinje3 = "test 3", land = "NO")),
        utfyllendeBeskrivelse = "Dette var dumt"
    )
}

fun skade(): Skade {
    return Skade(
        skadedeDeler = listOf(SkadetDel("kuldeskade", kroppsdelTabellD = "ribbenOgSkulderblad")),
        alvorlighetsgrad = "antattOppsoektLege",
        antattSykefravaerTabellH = "alternativenePasserIkke"
    )
}

fun skadelidt(): Skadelidt {
    return Skadelidt(
        norskIdentitetsnummer = "12345678910",
        dekningsforhold = Dekningsforhold(
            organisasjonsnummer = "123456789",
            stillingstittelTilDenSkadelidte = listOf("sivilingeniorerByggOgAnlegg"),
            rolletype = "arbeidstaker",
            navnPaaVirksomheten = "Test"
        )
    )
}