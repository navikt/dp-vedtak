package no.nav.dagpenger.vedtak.modell.vedtak

import no.nav.dagpenger.vedtak.modell.Dagpengerettighet
import no.nav.dagpenger.vedtak.modell.TemporalCollection
import no.nav.dagpenger.vedtak.modell.entitet.Beløp
import no.nav.dagpenger.vedtak.modell.entitet.Timer
import no.nav.dagpenger.vedtak.modell.hendelser.StansHendelse
import no.nav.dagpenger.vedtak.modell.hendelser.SøknadBehandletHendelse
import no.nav.dagpenger.vedtak.modell.mengde.Stønadsperiode
import no.nav.dagpenger.vedtak.modell.rapportering.LøpendeBehandling
import no.nav.dagpenger.vedtak.modell.rapportering.Rapporteringsperiode
import no.nav.dagpenger.vedtak.modell.vedtak.Vedtak.Companion.harBehandlet
import no.nav.dagpenger.vedtak.modell.visitor.VedtakHistorikkVisitor
import java.math.BigDecimal
import java.time.LocalDate

class VedtakHistorikk(historiskeVedtak: List<Vedtak> = listOf()) {

    private val vedtak = historiskeVedtak.sorted().toMutableList()
    private val observers = mutableSetOf<VedtakObserver>()
    internal val dagsatsHistorikk = TemporalCollection<BigDecimal>()

    internal val trukketEgenandelHistorikk = TrukketEgenandelHistorikk()
    internal val grunnlagHistorikk = TemporalCollection<BigDecimal>()
    internal val stønadsperiodeHistorikk = TemporalCollection<Stønadsperiode>()
    internal val forbrukHistorikk = ForbrukHistorikk()
    internal val dagpengerettighetHistorikk = TemporalCollection<Dagpengerettighet>()
    internal val vanligArbeidstidHistorikk = TemporalCollection<Timer>()
    internal val egenandelHistorikk = TemporalCollection<Beløp>()

    init {
        vedtak.forEach { it.populer(this) }
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
        this.leggTilVedtak(
            LøpendeBehandling(
                rapporteringsId = rapporteringsperiode.rapporteringsId,
                stønadsperiodeHistorikk = stønadsperiodeHistorikk,
                satsHistorikk = dagsatsHistorikk,
                dagpengerettighetHistorikk = dagpengerettighetHistorikk,
                vanligArbeidstidHistorikk = vanligArbeidstidHistorikk,
                egenandelHistorikk = egenandelHistorikk,
                forbrukHistorikk = forbrukHistorikk,
                trukketEgenandelHistorikk = trukketEgenandelHistorikk,
            ).håndter(rapporteringsperiode),
        )
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
            val gjenstående = stønadsperiodeHistorikk.get(LocalDate.now()) - forbrukHistorikk.summer(LocalDate.now())
            visitor.visitGjenståendeStønadsperiode(gjenstående)
        }
        if (egenandelHistorikk.harHistorikk()) {
            val gjenstående = egenandelHistorikk.get(LocalDate.now()) - trukketEgenandelHistorikk.summer(LocalDate.now())
            visitor.visitGjenståendeEgenandel(gjenstående)
        }

        visitor.preVisitVedtak()
        vedtak.forEach { it.accept(visitor) }
        visitor.postVisitVedtak()
    }

    private fun leggTilVedtak(vedtak: Vedtak) {
        this.vedtak.add(
            vedtak.also {
                it.populer(this)
            },
        )
        this.observers.forEach {
            it.vedtakFattet(
                VedtakFattetVisitor().apply(vedtak::accept).vedtakFattet,
            )
        }
    }
}
