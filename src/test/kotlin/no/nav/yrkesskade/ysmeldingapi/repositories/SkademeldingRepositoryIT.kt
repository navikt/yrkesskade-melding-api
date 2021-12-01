package no.nav.yrkesskade.ysmeldingapi.repositories

import no.nav.yrkesskade.ysmeldingapi.domain.Skademelding
import no.nav.yrkesskade.ysmeldingapi.repositories.testutils.docker.PostgresDockerContainer
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest
import org.springframework.test.context.ActiveProfiles

@DataJpaTest
@ActiveProfiles("integration")
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class SkademeldingRepositoryIT {

    @Autowired
    lateinit var repository: SkademeldingRepository

    init {
        PostgresDockerContainer.container
    }

    @Test
    fun `save one skademelding should return one skademelding`() {
        repository.save(
            Skademelding(
                null,
                """{"some": "data"}""",
            )
        )
        assertThat(repository.findAll().size).isEqualTo(1)
    }

    @Test
    fun `save two skademeldinger should return two skademeldinger`() {
        repository.save(
            Skademelding(
                null,
                """{"some": "data"}""",
            )
        )
        repository.save(
            Skademelding(
                null,
                """{"some more": "data"}""",
            )
        )
        assertThat(repository.findAll().size).isEqualTo(2)
    }

    @Test
    fun `empty database should return no skademelding`() {
        assertThat(repository.count()).isZero
    }
}