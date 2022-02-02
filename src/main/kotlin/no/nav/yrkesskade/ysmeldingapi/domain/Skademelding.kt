package no.nav.yrkesskade.ysmeldingapi.domain

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import no.nav.yrkesskade.ysmeldingapi.models.SkademeldingDto
import javax.persistence.Entity
import javax.persistence.GeneratedValue
import javax.persistence.GenerationType
import javax.persistence.Id

@Entity
open class Skademelding(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY) //Uses underlying persistence framework to generate an Id
    open var id: Int?,
    open var skademelding: String
) {
    fun toSkademeldingDto(): SkademeldingDto {
        return SkademeldingDto(
            this.id ?: 0,
            jacksonObjectMapper().readTree(skademelding)
        )
    }
}
