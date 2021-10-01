package no.nav.yrkesskade.ysmeldingapi.services

import no.nav.yrkesskade.ysmeldingapi.models.SkademeldingDto
import no.nav.yrkesskade.ysmeldingapi.repositories.SkademeldingRepository
import org.springframework.stereotype.Service

@Service
class SkademeldingService(private val skademeldingRepository: SkademeldingRepository) {

    fun behandleSkademelding(skademeldingDto: SkademeldingDto): SkademeldingDto {
        val lagretSkademelding = skademeldingRepository.save(skademeldingDto.toSkademelding())
        return lagretSkademelding.toSkademeldingDto()
    }
}