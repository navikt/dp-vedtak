package no.nav.dagpenger.vedtak.iverksetting

import io.kotest.matchers.maps.shouldContainAll
import io.kotest.matchers.shouldBe
import no.nav.dagpenger.aktivitetslogg.Aktivitet
import no.nav.dagpenger.vedtak.iverksetting.Iverksetting.Tilstand.TilstandNavn.AvventerIverksetting
import no.nav.dagpenger.vedtak.iverksetting.Iverksetting.Tilstand.TilstandNavn.Iverksatt
import no.nav.dagpenger.vedtak.iverksetting.Iverksetting.Tilstand.TilstandNavn.Mottatt
import no.nav.dagpenger.vedtak.iverksetting.hendelser.IverksattHendelse
import no.nav.dagpenger.vedtak.iverksetting.hendelser.VedtakFattetHendelse
import org.junit.jupiter.api.Test
import java.time.LocalDate
import java.time.LocalDateTime
import java.util.UUID

class IverksettingTest {

    private val ident = "12345678911"
    private val testObservatør = IverksettingObservatør()
    private val vedtakId = UUID.randomUUID()
    private val behandlingId = UUID.randomUUID()
    private val vedtakstidspunkt = LocalDateTime.now()
    private val virkningsdato = LocalDate.now()
    private val utfall = IverksettingsVedtak.Utfall.Innvilget

    private val iverksetting = Iverksetting(vedtakId, ident).also {
        it.addObserver(testObservatør)
    }
    private val inspektør get() = IverksettingInspektør(iverksetting)

    @Test
    fun `Skal starte iverksetting når vedtak fattes`() {
        iverksetting.håndter(
            VedtakFattetHendelse(
                ident = ident,
                iverksettingsVedtak = IverksettingsVedtak(
                    vedtakId = vedtakId,
                    behandlingId = behandlingId,
                    vedtakstidspunkt = vedtakstidspunkt,
                    virkningsdato = virkningsdato,
                    utfall = utfall,
                ),
            ),
        )

        assertBehov(
            IverksettingBehov.Iverksett,
            forventetDetaljer = mapOf(
                "ident" to ident,
                "vedtakId" to vedtakId.toString(),
                "behandlingId" to behandlingId,
                "vedtakstidspunkt" to vedtakstidspunkt,
                "virkningsdato" to virkningsdato,
                "utfall" to utfall,
                "iverksettingId" to inspektør.iverksettingId.toString(),
                "tilstand" to "Mottatt",
            ),
        )

        iverksetting.håndter(
            IverksattHendelse(ident = ident, iverksettingId = inspektør.iverksettingId, vedtakId = inspektør.vedtakId),
        )

        assertTilstander(
            Mottatt,
            AvventerIverksetting,
            Iverksatt,
        )
    }

    private fun assertTilstander(vararg tilstander: Iverksetting.Tilstand.TilstandNavn) {
        tilstander.asList() shouldBe testObservatør.tilstander
    }

    private fun assertBehov(behovtype: Aktivitet.Behov.Behovtype, forventetDetaljer: Map<String, Any> = emptyMap()) {
        val behov = inspektør.innsendingLogg.behov().findLast {
            it.type == behovtype
        } ?: throw AssertionError("Fant ikke behov $behovtype")

        forventetDetaljer shouldContainAll behov.detaljer() + behov.kontekst()
    }
}
