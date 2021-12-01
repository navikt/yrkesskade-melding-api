package no.nav.yrkesskade.ysmeldingapi.repositories

import no.nav.yrkesskade.ysmeldingapi.domain.Skademelding
import org.springframework.data.jpa.repository.JpaRepository

interface SkademeldingRepository: JpaRepository<Skademelding, Int> {

}