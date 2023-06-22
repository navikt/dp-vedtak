package no.nav.dagpenger.vedtak.mediator.vedtak

import io.kotest.matchers.shouldBe
import no.nav.dagpenger.vedtak.modell.Dagpengerettighet
import no.nav.dagpenger.vedtak.modell.Dagpengerettighet.Ordinær
import no.nav.dagpenger.vedtak.modell.entitet.Beløp
import no.nav.dagpenger.vedtak.modell.entitet.Beløp.Companion.beløp
import no.nav.dagpenger.vedtak.modell.entitet.Stønadsdager
import no.nav.dagpenger.vedtak.modell.entitet.Timer
import no.nav.dagpenger.vedtak.modell.entitet.Timer.Companion.timer
import no.nav.dagpenger.vedtak.modell.vedtak.Vedtak
import no.nav.dagpenger.vedtak.modell.vedtak.Vedtak.Fattet
import no.nav.dagpenger.vedtak.modell.visitor.VedtakVisitor
import org.junit.jupiter.api.Test
import java.math.BigDecimal
import java.time.LocalDate
import java.time.LocalDateTime
import java.util.UUID

class VedtakTest {

    @Test
    fun `Vedtak blir opprettet med tilstand Fattet og ender opp i FattetOgIverksatt`() {
        val innvilgetRammevedtak = innvilgetRammevedtak()
        assertTilstand(innvilgetRammevedtak, Fattet)
        // assertTilstand(innvilgetRammevedtak, Iverksatt)
    }

    private fun assertTilstand(vedtak: Vedtak, tilstand: Vedtak.Tilstand) {
        VedtakInspektør(vedtak).tilstand shouldBe tilstand
    }

    private fun innvilgetRammevedtak() = Vedtak.innvilgelse(
        behandlingId = UUID.randomUUID(),
        virkningsdato = LocalDate.MAX,
        grunnlag = BigDecimal.ONE,
        dagsats = BigDecimal.ONE,
        stønadsdager = Stønadsdager(dager = 260),
        dagpengerettighet = Ordinær,
        vanligArbeidstidPerDag = 8.timer,
        egenandel = 3000.beløp,
    )
}

internal class VedtakInspektør(vedtak: Vedtak) : VedtakVisitor {

    init {
        vedtak.accept(this)
    }

    lateinit var tilstand: Vedtak.Tilstand

    override fun visitRammevedtak(
        vedtakId: UUID,
        behandlingId: UUID,
        virkningsdato: LocalDate,
        vedtakstidspunkt: LocalDateTime,
        utfall: Boolean,
        grunnlag: BigDecimal,
        dagsats: BigDecimal,
        stønadsdager: Stønadsdager,
        vanligArbeidstidPerDag: Timer,
        dagpengerettighet: Dagpengerettighet,
        egenandel: Beløp,
        tilstand: Vedtak.Tilstand,
    ) {
        this.tilstand = tilstand
    }
}
