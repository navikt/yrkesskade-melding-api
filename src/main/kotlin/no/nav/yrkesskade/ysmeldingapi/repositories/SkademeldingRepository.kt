package no.nav.yrkesskade.ysmeldingapi.repositories

import no.nav.yrkesskade.ysmeldingapi.domain.SkademeldingEntity
import org.springframework.data.jpa.repository.JpaRepository

interface SkademeldingRepository: JpaRepository<SkademeldingEntity, Int> {

}