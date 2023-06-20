package no.nav.dagpenger.vedtak.modell.vedtak

import no.nav.dagpenger.vedtak.modell.Dagpengerettighet
import no.nav.dagpenger.vedtak.modell.TemporalCollection
import no.nav.dagpenger.vedtak.modell.entitet.Beløp
import no.nav.dagpenger.vedtak.modell.entitet.Stønadsdager
import no.nav.dagpenger.vedtak.modell.entitet.Timer
import no.nav.dagpenger.vedtak.modell.hendelser.StansHendelse
import no.nav.dagpenger.vedtak.modell.hendelser.SøknadBehandletHendelse
import no.nav.dagpenger.vedtak.modell.rapportering.LøpendeBehandling
import no.nav.dagpenger.vedtak.modell.rapportering.Rapporteringsperiode
import no.nav.dagpenger.vedtak.modell.utbetaling.LøpendeRettighetDag
import no.nav.dagpenger.vedtak.modell.vedtak.Vedtak.Companion.harBehandlet
import no.nav.dagpenger.vedtak.modell.visitor.VedtakHistorikkVisitor
import no.nav.dagpenger.vedtak.modell.visitor.VedtakVisitor
import java.math.BigDecimal
import java.time.LocalDate
import java.time.LocalDateTime
import java.util.SortedSet
import java.util.UUID

class VedtakHistorikk(historiskeVedtak: List<Vedtak> = listOf()) {

    private val vedtak: SortedSet<Vedtak> = historiskeVedtak.toSortedSet()
    private val observers = mutableSetOf<VedtakObserver>()

    internal val vanligArbeidstidHistorikk = TemporalCollection<Timer>()
    internal val grunnlagHistorikk = TemporalCollection<BigDecimal>()
    internal val dagsatsHistorikk = TemporalCollection<BigDecimal>()
    internal val dagpengerettighetHistorikk = TemporalCollection<Dagpengerettighet>()

    internal val stønadsdagerHistorikk = TemporalCollection<Stønadsdager>()
    internal val forbrukHistorikk = ForbrukHistorikk()
    internal val egenandelHistorikk = TemporalCollection<Beløp>()
    internal val trukketEgenandelHistorikk = TrukketEgenandelHistorikk()
    internal val beløpTilUtbetalingHistorikk = TemporalCollection<Beløp>()

    init {
        vedtak.forEach { HistorikkOppdaterer(this).apply(it::accept) }
    }

    fun håndter(søknadBehandletHendelse: SøknadBehandletHendelse) {
        if (vedtak.harBehandlet(søknadBehandletHendelse.behandlingId)) {
            søknadBehandletHendelse.info("Har allerede behandlet SøknadBehandletHendelse")
            return
        }
        val vedtak = søknadBehandletHendelse.tilVedtak()
        leggTilVedtak(vedtak)
    }

    fun håndter(rapporteringsperiode: Rapporteringsperiode) {
        // @todo: Sjekk at vi ikke har behandlet denne før..
        if (vanligArbeidstidHistorikk.harHistorikk()) {
            this.leggTilVedtak(
                vedtak = LøpendeBehandling(
                    rapporteringsId = rapporteringsperiode.rapporteringsId,
                    stønadsdagerHistorikk = stønadsdagerHistorikk,
                    satsHistorikk = dagsatsHistorikk,
                    dagpengerettighetHistorikk = dagpengerettighetHistorikk,
                    vanligArbeidstidHistorikk = vanligArbeidstidHistorikk,
                    egenandelHistorikk = egenandelHistorikk,
                    forbrukHistorikk = forbrukHistorikk,
                    trukketEgenandelHistorikk = trukketEgenandelHistorikk,
                ).håndter(rapporteringsperiode),
            )
        }
    }

    fun håndter(stansHendelse: StansHendelse) {
        if (vedtak.harBehandlet(stansHendelse.behandlingId)) {
            stansHendelse.info("Har allerede behandlet StansHendelse")
            return
        }
        this.leggTilVedtak(
            stansHendelse.tilVedtak(),
        )
    }

    fun addObserver(vedtakObserver: VedtakObserver) {
        this.observers.add(vedtakObserver)
    }

    fun accept(visitor: VedtakHistorikkVisitor) {
        if (forbrukHistorikk.harHistorikk()) {
            val gjenstående = stønadsdagerHistorikk.get(LocalDate.now()) - forbrukHistorikk.summer(LocalDate.now())
            visitor.visitGjenståendeStønadsperiode(gjenstående)
        }
        if (egenandelHistorikk.harHistorikk()) {
            val gjenstående =
                egenandelHistorikk.get(LocalDate.now()) - trukketEgenandelHistorikk.summer(LocalDate.now())
            visitor.visitGjenståendeEgenandel(gjenstående)
        }

        visitor.preVisitVedtak()
        vedtak.forEach { it.accept(visitor) }
        visitor.postVisitVedtak()
    }

    fun gjenståendeStønadsdagerFra(dato: LocalDate): Stønadsdager =
        stønadsdagerHistorikk.get(dato) - forbrukHistorikk.summer(dato)

    fun gjenståendeEgenandelFra(dato: LocalDate): Beløp =
        egenandelHistorikk.get(dato) - trukketEgenandelHistorikk.summer(dato)

    fun beløpTilUtbetalingFor(dato: LocalDate): Beløp = beløpTilUtbetalingHistorikk.get(dato)

    private fun leggTilVedtak(vedtak: Vedtak) {
        this.vedtak.add(
            vedtak.also {
                HistorikkOppdaterer(this).apply(it::accept)
            },
        )
        this.observers.forEach { vedtakObserver ->
            when (vedtak) {
                is LøpendeRettighetVedtak -> {
                    val løpendeVedtakFattet = VedtakFattetVisitor().apply(vedtak::accept).løpendeVedtakFattet
                    vedtakObserver.løpendeVedtakFattet(løpendeVedtakFattet)
                }

                else -> vedtakObserver.vedtakFattet(VedtakFattetVisitor().apply(vedtak::accept).vedtakFattet)
            }
        }
    }

    private class HistorikkOppdaterer(private val vedtakHistorikk: VedtakHistorikk) : VedtakVisitor {

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
        ) {
            vedtakHistorikk.dagsatsHistorikk.put(virkningsdato, dagsats)
            vedtakHistorikk.grunnlagHistorikk.put(virkningsdato, grunnlag)
            vedtakHistorikk.stønadsdagerHistorikk.put(virkningsdato, stønadsdager)
            vedtakHistorikk.dagpengerettighetHistorikk.put(virkningsdato, dagpengerettighet)
            vedtakHistorikk.vanligArbeidstidHistorikk.put(virkningsdato, vanligArbeidstidPerDag)
            vedtakHistorikk.egenandelHistorikk.put(virkningsdato, egenandel)
        }

        override fun visitLøpendeRettighet(
            vedtakId: UUID,
            behandlingId: UUID,
            vedtakstidspunkt: LocalDateTime,
            utfall: Boolean,
            virkningsdato: LocalDate,
            forbruk: Stønadsdager,
            trukketEgenandel: Beløp,
            beløpTilUtbetaling: Beløp,
            rettighetsdager: List<LøpendeRettighetDag>,
        ) {
            vedtakHistorikk.forbrukHistorikk.put(virkningsdato, forbruk)
            vedtakHistorikk.trukketEgenandelHistorikk.put(virkningsdato, trukketEgenandel)
            vedtakHistorikk.beløpTilUtbetalingHistorikk.put(virkningsdato, beløpTilUtbetaling)
        }

        override fun visitAvslag(
            vedtakId: UUID,
            behandlingId: UUID,
            vedtakstidspunkt: LocalDateTime,
            utfall: Boolean,
            virkningsdato: LocalDate,
        ) {
        }

        override fun visitStans(
            vedtakId: UUID,
            behandlingId: UUID,
            virkningsdato: LocalDate,
            vedtakstidspunkt: LocalDateTime,
            utfall: Boolean,
        ) {
            vedtakHistorikk.dagpengerettighetHistorikk.put(virkningsdato, Dagpengerettighet.Ingen)
        }
    }
}
