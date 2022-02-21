package no.nav.yrkesskade.ysmeldingapi.domain

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import no.nav.yrkesskade.ysmeldingapi.models.SkademeldingDto
import java.util.*
import javax.persistence.*

@Entity
@Table(name = "skademelding")
open class SkademeldingEntity(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY) //Uses underlying persistence framework to generate an Id
    open var id: Int?,
    open var skademelding: String,
    open var kilde: String,
    open var mottattTidspunkt: Date
) {
    fun toSkademeldingDto(): SkademeldingDto {
        return SkademeldingDto(
            this.id ?: 0,
            jacksonObjectMapper().readTree(skademelding),
            this.kilde,
            this.mottattTidspunkt
        )
    }
}
