package no.nav.dagpenger.vedtak.mediator.vedtak

import com.fasterxml.jackson.databind.JsonNode
import io.kotest.assertions.assertSoftly
import io.kotest.matchers.shouldBe
import io.kotest.matchers.string.shouldNotBeBlank
import no.nav.dagpenger.vedtak.modell.vedtak.VedtakObserver
import no.nav.dagpenger.vedtak.modell.vedtak.VedtakObserver.Utfall.Avslått
import no.nav.dagpenger.vedtak.modell.vedtak.VedtakObserver.Utfall.Innvilget
import no.nav.helse.rapids_rivers.testsupport.TestRapid
import org.junit.jupiter.api.Test
import java.time.LocalDate
import java.time.LocalDateTime
import java.util.UUID

class VedtakFattetObserverTest {
    private val testRapid = TestRapid()
    private val vedtakFattetObserver = VedtakFattetObserver(testRapid)

    @Test
    fun `Skal sende ut melding på rapiden om at vedtak er fattet`() {
        vedtakFattetObserver.vedtakFattet(
            ident = "1234568901",
            vedtakFattet(utfall = Innvilget),
        )

        vedtakFattetObserver.vedtakFattet(
            ident = "1234568901",
            vedtakFattet(utfall = Avslått),
        )

        testRapid.inspektør.size shouldBe 2
        val message1 = testRapid.inspektør.message(0)
        val message2 = testRapid.inspektør.message(1)

        assertSoftly {
            message1["@event_name"].asText() shouldBe "dagpenger_innvilget"
            assertVedtakInformasjon(message1)
            message2["@event_name"].asText() shouldBe "dagpenger_avslått"
            assertVedtakInformasjon(message2)
        }
    }

    @Test
    fun `Skal sende ut melding på rapiden om at utbetalingsvedtak er fattet`() {
        vedtakFattetObserver.utbetalingsvedtakFattet(
            ident = "1234568901",
            utbetalingsvedtakFattet(utfall = Innvilget),
        )

        vedtakFattetObserver.utbetalingsvedtakFattet(
            ident = "1234568901",
            utbetalingsvedtakFattet(utfall = Avslått),
        )

        testRapid.inspektør.size shouldBe 2
        val message1 = testRapid.inspektør.message(0)
        val message2 = testRapid.inspektør.message(1)

        assertSoftly {
            message1["@event_name"].asText() shouldBe "utbetaling_vedtak_fattet"
            assertVedtakInformasjon(message1)
            message2["@event_name"].asText() shouldBe "utbetaling_vedtak_fattet"
            assertVedtakInformasjon(message2)
        }
    }

    private fun assertVedtakInformasjon(json: JsonNode) {
        json["ident"].asText() shouldBe "1234568901"
        json["behandlingId"].asText().shouldNotBeBlank()
        json["vedtakId"].asText().shouldNotBeBlank()
        json["sakId"].asText().shouldNotBeBlank()
        json["vedtaktidspunkt"].asText().shouldNotBeBlank()
        json["virkningsdato"].asText().shouldNotBeBlank()
    }

    private fun vedtakFattet(utfall: VedtakObserver.Utfall) = VedtakObserver.VedtakFattet(
        vedtakId = UUID.randomUUID(),
        sakId = UUID.randomUUID().toString(),
        vedtakstidspunkt = LocalDateTime.now(),
        behandlingId = UUID.randomUUID(),
        virkningsdato = LocalDate.now(),
        utfall = utfall,
    )

    private fun utbetalingsvedtakFattet(utfall: VedtakObserver.Utfall) = VedtakObserver.UtbetalingsvedtakFattet(
        vedtakId = UUID.randomUUID(),
        sakId = UUID.randomUUID().toString(),
        behandlingId = UUID.randomUUID(),
        vedtakstidspunkt = LocalDateTime.now(),
        virkningsdato = LocalDate.now(),
        utfall = utfall,
        utbetalingsdager = emptyList(),
    )
}
