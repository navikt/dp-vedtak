package no.nav.dagpenger.vedtak.modell

import no.nav.dagpenger.vedtak.modell.entitet.Timer.Companion.timer
import no.nav.dagpenger.vedtak.modell.mengde.Enhet.Companion.arbeidsuker
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import java.time.LocalDate
import java.util.UUID

internal class VedtakHistorikkTest {

    @Test
    fun `Vedtakshistorikk skal kunne svare på om man har vedtak på et gitt tidspunkt`() {
        val vedtakHistorikk = VedtakHistorikk()

        assertFalse(vedtakHistorikk.harVedtak())
        val vedtak = Vedtak.innvilgelse(
            behandlingId = UUID.randomUUID(),
            virkningsdato = LocalDate.now(),
            grunnlag = 1000.toBigDecimal(),
            dagsats = 500.toBigDecimal(),
            stønadsperiode = 52.arbeidsuker,
            dagpengerettighet = Dagpengerettighet.Ordinær,
            vanligArbeidstidPerDag = 8.timer,
        )
        vedtakHistorikk.leggTilVedtak(vedtak)

        assertTrue(vedtakHistorikk.harVedtak())

        assertFalse(vedtakHistorikk.harVedtak(LocalDate.now().minusDays(1)))
    }
}
