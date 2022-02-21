package no.nav.yrkesskade.ysmeldingapi.client.mottak

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import no.nav.yrkesskade.ysmeldingapi.fixtures.enkelSkademelding
import no.nav.yrkesskade.ysmeldingapi.model.Skademelding
import no.nav.yrkesskade.ysmeldingapi.models.SkademeldingInnsendtHendelse
import no.nav.yrkesskade.ysmeldingapi.models.SkademeldingMetadata
import no.nav.yrkesskade.ysmeldingapi.test.AbstractIT
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import java.util.*
import java.util.concurrent.TimeUnit

@SpringBootTest
class MottakClientIT : AbstractIT() {

    @Autowired
    private lateinit var skademeldingInnsendingClient: SkademeldingInnsendingClient

    @Autowired
    private lateinit var mottakConsumer: SkademeldingInnsendtKafkaConsumer

    @Test
    fun `send melding til mottak`() {
        val skademelding: Skademelding = jacksonObjectMapper().treeToValue(enkelSkademelding().skademelding, Skademelding::class.java)
        val skademeldingInnsendtHendelse = SkademeldingInnsendtHendelse(
            skademelding = skademelding,
            metadata = SkademeldingMetadata("test", Date())
        )
        assertThat(skademeldingInnsendtHendelse.skademelding).isNotNull()
        assertThat(skademeldingInnsendtHendelse.metadata.kilde).isEqualTo("test");
        skademeldingInnsendingClient.sendTilMottak(skademeldingInnsendtHendelse)
        mottakConsumer.getLatch().await(10000, TimeUnit.MILLISECONDS);
        assertThat(mottakConsumer.getPayload()).contains("\"foedselsnummer\":3093242309");
        assertThat(mottakConsumer.getPayload()).contains("\"kilde\":\"${skademeldingInnsendtHendelse.metadata.kilde}\"");
    }
}