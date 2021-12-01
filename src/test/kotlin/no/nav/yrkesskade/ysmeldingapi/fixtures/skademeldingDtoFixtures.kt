package no.nav.yrkesskade.ysmeldingapi.fixtures

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import no.nav.yrkesskade.ysmeldingapi.models.SkademeldingDto
import java.nio.file.Files
import java.nio.file.Path

fun enkelSkademelding(): SkademeldingDto {
    val skademelding = Files.readString(Path.of("src/test/resources/skademeldinger/enkelSkademelding.json"))
    return SkademeldingDto(
        null,
        jacksonObjectMapper().readTree(skademelding)
    )
}