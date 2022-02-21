package no.nav.yrkesskade.ysmeldingapi.fixtures

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import no.nav.yrkesskade.ysmeldingapi.model.Innmelder
import no.nav.yrkesskade.ysmeldingapi.model.Innmelderrolle
import no.nav.yrkesskade.ysmeldingapi.model.Skademelding
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
        foedselsnummer = 3093242309,
        fornavn = "",
        etternavn = "",
        mellomnavn = "",
        paaVegneAvOrgnr = "123456789",
        innmelderrolle = Innmelderrolle.arbeidsgiverrepresentant,
        altinnrolle = null
    )
}