package no.nav.yrkesskade.ysmeldingapi.client.mottak

import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import no.nav.yrkesskade.model.SkademeldingInnsendtHendelse
import no.nav.yrkesskade.model.SkademeldingMetadata
import no.nav.yrkesskade.model.Spraak
import no.nav.yrkesskade.skademelding.model.Skademelding
import no.nav.yrkesskade.ysmeldingapi.fixtures.enkelSkademelding
import no.nav.yrkesskade.ysmeldingapi.test.AbstractIT
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import java.time.Instant
import java.util.concurrent.TimeUnit

@SpringBootTest
class SkademeldingInnsendtClientIT : AbstractIT() {

    @Autowired
    private lateinit var skademeldingInnsendingClient: SkademeldingInnsendingClient

    private val objectMapper = jacksonObjectMapper().registerModule(JavaTimeModule())

    @Test
    fun `send melding til mottak`() {
        val skademelding: Skademelding = objectMapper.treeToValue(enkelSkademelding().skademelding, Skademelding::class.java)
        val skademeldingInnsendtHendelse = SkademeldingInnsendtHendelse(
            skademelding = skademelding,
            metadata = SkademeldingMetadata(
                kilde = "test",
                tidspunktMottatt = Instant.now(),
                spraak = Spraak.NB
            )
        )
        assertThat(skademeldingInnsendtHendelse.skademelding).isNotNull()
        assertThat(skademeldingInnsendtHendelse.metadata.kilde).isEqualTo("test")
        skademeldingInnsendingClient.sendTilMottak(skademeldingInnsendtHendelse)
        mottakConsumer.getLatch().await(10000, TimeUnit.MILLISECONDS)
        assertThat(mottakConsumer.getPayload()).contains("\"norskIdentitetsnummer\":3093242309")
        assertThat(mottakConsumer.getPayload()).contains("\"kilde\":\"${skademeldingInnsendtHendelse.metadata.kilde}\"")
    }
}