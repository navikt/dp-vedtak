package no.nav.dagpenger.vedtak.modell

import no.nav.dagpenger.vedtak.modell.entitet.Timer.Companion.timer
import no.nav.dagpenger.vedtak.modell.mengde.Enhet.Companion.arbeidsuker
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import java.math.BigDecimal
import java.time.LocalDate
import java.util.UUID

internal class VedtakHistorikkTest {

    @Test
    fun `Gir oversikt over gjeldende fakta fra vedtak for en gitt dato`() {
        val vedtakHistorikk = VedtakHistorikk()
        val dagsats = 600.toBigDecimal()

        vedtakHistorikk.leggTilVedtak(rammevedtak(dagsats))

        assertEquals(dagsats, vedtakHistorikk.dagsatshistorikk.get(LocalDate.now()))
    }

    @Test
    fun `Vedtakshistorikk skal kunne svare på om man har vedtak på et gitt tidspunkt`() {
        val vedtakHistorikk = VedtakHistorikk()
        assertFalse(vedtakHistorikk.harVedtak())

        vedtakHistorikk.leggTilVedtak(rammevedtak(dagsats = 500.toBigDecimal()))

        assertTrue(vedtakHistorikk.harVedtak())

        assertFalse(vedtakHistorikk.harVedtak(LocalDate.now().minusDays(1)))
    }

    private fun rammevedtak(dagsats: BigDecimal) = Vedtak.innvilgelse(
        behandlingId = UUID.randomUUID(),
        virkningsdato = LocalDate.now(),
        grunnlag = 1000.toBigDecimal(),
        dagsats = dagsats,
        stønadsperiode = 52.arbeidsuker,
        dagpengerettighet = Dagpengerettighet.Ordinær,
        vanligArbeidstidPerDag = 8.timer,
        antallVenteDager = 3.toDouble(),
    )
}
