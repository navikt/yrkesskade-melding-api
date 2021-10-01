package no.nav.yrkesskade.ysmeldingapi.domain

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import no.nav.yrkesskade.ysmeldingapi.models.SkademeldingDto
import java.time.Instant
import javax.persistence.Entity
import javax.persistence.GeneratedValue
import javax.persistence.GenerationType
import javax.persistence.Id

@Entity
data class Skademelding(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY) //Uses underlying persistence framework to generate an Id
    var id: Int?,

    var json: String,
    var changedBy: String,
    var changedTime: Instant
) {
    fun toSkademeldingDto(): SkademeldingDto {
        return jacksonObjectMapper().readValue(json)
    }
}
