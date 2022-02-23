package no.nav.yrkesskade.ysmeldingapi.repositories

import no.nav.yrkesskade.ysmeldingapi.domain.SkademeldingEntity
import no.nav.yrkesskade.ysmeldingapi.test.AbstractIT
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase
import org.springframework.boot.test.context.SpringBootTest
import java.time.Instant

@SpringBootTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class SkademeldingRepositoryIT : AbstractIT() {

    @Autowired
    lateinit var repository: SkademeldingRepository

    @Test
    fun `save one skademelding should return one skademelding`() {
        repository.save(
            SkademeldingEntity(
                null,
                """{"some": "data"}""",
                "test-kilde",
                Instant.now()
            )
        )
        assertThat(repository.findAll().size).isEqualTo(1)
    }

    @Test
    fun `save two skademeldinger should return two skademeldinger`() {
        repository.save(
            SkademeldingEntity(
                null,
                """{"some": "data"}""",
                "test-kilde",
                Instant.now()
            )
        )
        repository.save(
            SkademeldingEntity(
                null,
                """{"some more": "data"}""",
                "test-kilde",
                Instant.now()
            )
        )
        assertThat(repository.findAll().size).isEqualTo(2)
    }

    @Test
    fun `empty database should return no skademelding`() {
        assertThat(repository.count()).isZero
    }
}