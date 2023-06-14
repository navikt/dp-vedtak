package no.nav.dagpenger.vedtak.mediator.vedtak

import io.kotest.assertions.assertSoftly
import io.kotest.matchers.shouldBe
import io.kotest.matchers.string.shouldNotBeBlank
import no.nav.dagpenger.vedtak.modell.vedtak.VedtakObserver
import no.nav.dagpenger.vedtak.modell.vedtak.VedtakObserver.Utfall.Innvilget
import no.nav.helse.rapids_rivers.testsupport.TestRapid
import org.junit.jupiter.api.Test
import java.time.LocalDate
import java.time.LocalDateTime
import java.util.UUID

class VedtakFattetKafkaObserverTest {
    private val testRapid = TestRapid()
    private val vedtakFattetKafkaObserver = VedtakFattetKafkaObserver(testRapid)

    @Test
    fun `Skal sende ut melding om at vedtak er fattet på rapiden`() {
        val vedtakId = UUID.randomUUID()
        val behandlingId = UUID.randomUUID()
        val vedtakstidspunkt = LocalDateTime.now()
        val virkningsdato = LocalDate.now()
        vedtakFattetKafkaObserver.vedtakFattet(
            ident = "1234568901",
            VedtakObserver.VedtakFattet(
                vedtakId,
                vedtakstidspunkt = vedtakstidspunkt,
                behandlingId = behandlingId,
                virkningsdato = virkningsdato,
                utfall = Innvilget,
            ),
        )
        testRapid.inspektør.size shouldBe 1
        val message = testRapid.inspektør.message(0)

        assertSoftly {
            message["@event_name"].asText() shouldBe "vedtak_fattet"
            message["ident"].asText() shouldBe "1234568901"
            message["behandlingId"].asText() shouldBe behandlingId.toString()
            message["vedtakId"].asText() shouldBe vedtakId.toString()
            message["vedtaktidspunkt"].asText().shouldNotBeBlank()
            message["virkningsdato"].asText() shouldBe virkningsdato.toString()
            message["utfall"].asText() shouldBe "Innvilget"
        }
    }
}
