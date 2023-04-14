package no.nav.dagpenger.vedtak.modell.vedtak

import no.nav.dagpenger.vedtak.modell.Dagpengerettighet
import no.nav.dagpenger.vedtak.modell.TemporalCollection
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

internal class VedtakHistorikk(historiskeVedtak: List<Vedtak> = listOf()) {

    private val observers = mutableListOf<VedtakObserver>()

    private val vedtak = historiskeVedtak.toMutableList()

    internal val dagsatsHistorikk = TemporalCollection<BigDecimal>()
    internal val grunnlagHistorikk = TemporalCollection<BigDecimal>()
    internal val stønadsperiodeHistorikk = TemporalCollection<Stønadsperiode>()
    internal val gjenståendeStønadsperiodeHistorikk = TemporalCollection<Stønadsperiode>()
    internal val dagpengerettighetHistorikk = TemporalCollection<Dagpengerettighet>()
    internal val vanligArbeidstidHistorikk = TemporalCollection<Timer>()
    internal val ventetidHistorikk = TemporalCollection<Timer>()
    internal val gjenståendeVentetidHistorikk = TemporalCollection<Timer>()

    init {
        vedtak.forEach { it.populer(this) }
    }

    fun håndter(søknadBehandletHendelse: SøknadBehandletHendelse) {
        if (vedtak.harBehandlet(søknadBehandletHendelse.behandlingId)) {
            søknadBehandletHendelse.warn("Har allerede behandlet SøknadBehandletHendelse")
            return
        }
        val vedtak = søknadBehandletHendelse.tilVedtak()
        leggTilVedtak(vedtak)
    }

    fun håndter(rapporteringsperiode: Rapporteringsperiode) {
        this.leggTilVedtak(
            LøpendeBehandling(
                rapporteringsId = rapporteringsperiode.rapporteringsId,
                satsHistorikk = dagsatsHistorikk,
                dagpengerettighetHistorikk = dagpengerettighetHistorikk,
                vanligArbeidstidHistorikk = vanligArbeidstidHistorikk,
                gjenståendeVentetidHistorikk = gjenståendeVentetidHistorikk,
            ).håndter(rapporteringsperiode),
        )
    }

    fun håndter(stansHendelse: StansHendelse) {
        if (vedtak.harBehandlet(stansHendelse.behandlingId)) {
            stansHendelse.warn("Har allerede behandlet StansHendelse")
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
        if (gjenståendeStønadsperiodeHistorikk.harHistorikk()) {
            visitor.visitGjenståendeStønadsperiode(gjenståendeStønadsperiodeHistorikk.get(LocalDate.now()))
            visitor.visitGjenståendeVentetid(gjenståendeVentetidHistorikk.get(LocalDate.now()))
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
