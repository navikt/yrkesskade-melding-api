package no.nav.yrkesskade.ysmeldingapi.fixtures

import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import no.nav.yrkesskade.skademelding.model.Innmelder
import no.nav.yrkesskade.skademelding.model.Innmelderrolle
import no.nav.yrkesskade.skademelding.model.Skademelding
import no.nav.yrkesskade.ysmeldingapi.models.SkademeldingDto
import java.nio.file.Files
import java.nio.file.Path
import java.time.Instant

private val objectMapper = jacksonObjectMapper().registerModule(JavaTimeModule())

fun enkelSkademelding(): SkademeldingDto {
    val skademelding = Files.readString(Path.of("src/test/resources/skademeldinger/enkelSkademelding.json"))
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
        skadelidt = null,
        skade = null,
        hendelsesfakta = null
    )
}

fun arbeidsgiverInnmelder(): Innmelder {
    return Innmelder(
        norskIdentitetsnummer = "3093242309",
        paaVegneAv = "910521551",
        innmelderrolle = Innmelderrolle.virksomhetsrepresentant,
        altinnrolleIDer = null
    )
}