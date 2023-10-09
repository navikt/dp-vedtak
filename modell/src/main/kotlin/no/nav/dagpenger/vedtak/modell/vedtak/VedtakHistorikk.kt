package no.nav.dagpenger.vedtak.modell.vedtak

import no.nav.dagpenger.vedtak.modell.SakId
import no.nav.dagpenger.vedtak.modell.TemporalCollection
import no.nav.dagpenger.vedtak.modell.entitet.Beløp
import no.nav.dagpenger.vedtak.modell.entitet.Periode
import no.nav.dagpenger.vedtak.modell.entitet.Stønadsdager
import no.nav.dagpenger.vedtak.modell.entitet.Timer
import no.nav.dagpenger.vedtak.modell.utbetaling.Utbetalingsdag
import no.nav.dagpenger.vedtak.modell.vedtak.Vedtak.Companion.harBehandlet
import no.nav.dagpenger.vedtak.modell.vedtak.rettighet.Hovedrettighet
import no.nav.dagpenger.vedtak.modell.vedtak.rettighet.Ordinær
import no.nav.dagpenger.vedtak.modell.vedtak.rettighet.Permittering
import no.nav.dagpenger.vedtak.modell.vedtak.rettighet.PermitteringFraFiskeindustrien
import no.nav.dagpenger.vedtak.modell.visitor.VedtakHistorikkVisitor
import no.nav.dagpenger.vedtak.modell.visitor.VedtakVisitor
import java.time.LocalDate
import java.time.LocalDateTime
import java.util.UUID

class VedtakHistorikk internal constructor(private val vedtak: MutableList<Vedtak>) {

    internal constructor() : this(mutableListOf<Vedtak>())

    private val observers = mutableSetOf<VedtakObserver>()

    internal val vanligArbeidstidHistorikk = TemporalCollection<Timer>()
    internal val dagsatsHistorikk = TemporalCollection<Beløp>()

    internal val hovedrettighetHistorikk = TemporalCollection<Hovedrettighet>()

    internal val stønadsdagerHistorikk = TemporalCollection<Stønadsdager>()
    internal val forbrukHistorikk = ForbrukHistorikk()
    private val beløpTilUtbetalingHistorikk = TemporalCollection<Beløp>()

    init {
        vedtak.forEach { HistorikkOppdaterer(this).apply(it::accept) }
    }
    fun addObserver(vedtakObserver: VedtakObserver) {
        this.observers.add(vedtakObserver)
    }

    fun accept(visitor: VedtakHistorikkVisitor) {
        visitor.preVisitVedtak()
        vedtak.forEach { it.accept(visitor) }
        visitor.postVisitVedtak()
    }

    fun gjenståendeStønadsdagerFra(dato: LocalDate): Stønadsdager =
        stønadsdagerHistorikk.get(dato) - forbrukHistorikk.summer(dato)

    fun beløpTilUtbetalingFor(dato: LocalDate): Beløp = beløpTilUtbetalingHistorikk.get(dato)

    internal fun leggTilVedtak(vedtak: Vedtak) {
        this.vedtak.add(
            vedtak.also {
                HistorikkOppdaterer(this).apply(it::accept)
            },
        )
        this.observers.forEach { vedtakObserver ->
            when (vedtak) {
                is Utbetalingsvedtak -> {
                    val utbetalingVedtakFattet = VedtakFattetVisitor().apply(vedtak::accept).utbetalingsvedtakFattet
                    vedtakObserver.utbetalingsvedtakFattet(utbetalingVedtakFattet)
                }

                else -> vedtakObserver.vedtakFattet(VedtakFattetVisitor().apply(vedtak::accept).vedtakFattet)
            }
        }
    }

    internal fun harBehandlet(behandlingId: UUID) = this.vedtak.harBehandlet(behandlingId)

    private class HistorikkOppdaterer(private val vedtakHistorikk: VedtakHistorikk) : VedtakVisitor {

        private var virkningsdato: LocalDate? = null

        private fun virkningsdato() = requireNotNull(virkningsdato) { " Forventet at virkninsdato er satt. Har du husket preVisitVedtak?" }
        override fun preVisitVedtak(
            vedtakId: UUID,
            sakId: SakId,
            behandlingId: UUID,
            virkningsdato: LocalDate,
            vedtakstidspunkt: LocalDateTime,
            type: Vedtak.VedtakType,
        ) {
            this.virkningsdato = virkningsdato
        }

        override fun visitOrdinær(ordinær: Ordinær) {
            vedtakHistorikk.hovedrettighetHistorikk.put(virkningsdato(), ordinær)
        }

        override fun visitPermitteringFraFiskeindustrien(permitteringFraFiskeindustrien: PermitteringFraFiskeindustrien) {
            vedtakHistorikk.hovedrettighetHistorikk.put(virkningsdato(), permitteringFraFiskeindustrien)
        }

        override fun visitPermittering(permittering: Permittering) {
            vedtakHistorikk.hovedrettighetHistorikk.put(virkningsdato(), permittering)
        }

        override fun visitAntallStønadsdager(dager: Stønadsdager) {
            vedtakHistorikk.stønadsdagerHistorikk.put(virkningsdato(), dager)
        }

        override fun visitVanligArbeidstidPerDag(timer: Timer) {
            vedtakHistorikk.vanligArbeidstidHistorikk.put(virkningsdato(), timer)
        }

        override fun visitDagsats(beløp: Beløp) {
            vedtakHistorikk.dagsatsHistorikk.put(virkningsdato(), beløp)
        }

        override fun postVisitVedtak(
            vedtakId: UUID,
            sakId: SakId,
            behandlingId: UUID,
            virkningsdato: LocalDate,
            vedtakstidspunkt: LocalDateTime,
            type: Vedtak.VedtakType,
        ) {
            this.virkningsdato = null
        }

        override fun visitUtbetalingsvedtak(
            vedtakId: UUID,
            periode: Periode,
            utfall: Boolean,
            forbruk: Stønadsdager,
            beløpTilUtbetaling: Beløp,
            utbetalingsdager: List<Utbetalingsdag>,
        ) {
            vedtakHistorikk.forbrukHistorikk.put(this.virkningsdato(), forbruk)
            vedtakHistorikk.beløpTilUtbetalingHistorikk.put(this.virkningsdato(), beløpTilUtbetaling)
        }

        override fun visitAvslag(
            vedtakId: UUID,
            sakId: SakId,
            behandlingId: UUID,
            vedtakstidspunkt: LocalDateTime,
            utfall: Boolean,
            virkningsdato: LocalDate,
        ) {
            // todo: Skal hovedrettigheten være med?
        }

        override fun visitStans(
            vedtakId: UUID,
            sakId: SakId,
            behandlingId: UUID,
            virkningsdato: LocalDate,
            vedtakstidspunkt: LocalDateTime,
            utfall: Boolean?,
        ) {
            // todo: Hvilken rettighet stanses?
            vedtakHistorikk.hovedrettighetHistorikk.put(virkningsdato, Ordinær(false))
        }
    }
}
