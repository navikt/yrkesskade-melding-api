package no.nav.yrkesskade.ysmeldingapi.fixtures

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import no.nav.yrkesskade.skademelding.model.Innmelder
import no.nav.yrkesskade.skademelding.model.Innmelderrolle
import no.nav.yrkesskade.skademelding.model.Skademelding
import no.nav.yrkesskade.ysmeldingapi.models.SkademeldingDto
import java.nio.file.Files
import java.nio.file.Path
import java.util.*

fun enkelSkademelding(): SkademeldingDto {
    val skademelding = Files.readString(Path.of("src/test/resources/skademeldinger/enkelSkademelding.json"))
    return SkademeldingDto(
        null,
        jacksonObjectMapper().valueToTree(fullSkademelding()),
        "test-kilde",
        Date()
    )
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
        norskIdentitetsnummer = 3093242309,
        paaVegneAv = "123456789",
        innmelderrolle = Innmelderrolle.virksomhetsrepresentant,
        altinnrolleIDer = null
    )
}