package no.nav.dagpenger.vedtak.modell.vedtak

import no.nav.dagpenger.vedtak.modell.Dagpengerettighet
import no.nav.dagpenger.vedtak.modell.TemporalCollection
import no.nav.dagpenger.vedtak.modell.entitet.Timer
import no.nav.dagpenger.vedtak.modell.hendelser.SøknadBehandletHendelse
import no.nav.dagpenger.vedtak.modell.mengde.Stønadsperiode
import no.nav.dagpenger.vedtak.modell.rapportering.LøpendeBehandling
import no.nav.dagpenger.vedtak.modell.rapportering.Rapporteringsperiode
import no.nav.dagpenger.vedtak.modell.vedtak.Vedtak.Companion.finn
import no.nav.dagpenger.vedtak.modell.visitor.VedtakHistorikkVisitor
import java.math.BigDecimal
import java.time.LocalDate

internal class VedtakHistorikk(historiskeVedtak: List<Vedtak> = listOf()) {

    private val observers = mutableListOf<VedtakObserver>()

    private val vedtak = historiskeVedtak.toMutableList()

    internal val dagsatshistorikk = TemporalCollection<BigDecimal>()
    internal val grunnlaghistorikk = TemporalCollection<BigDecimal>()
    internal val stønadsperiodehistorikk = TemporalCollection<Stønadsperiode>()
    internal val gjenståendeStønadsperiode = TemporalCollection<Stønadsperiode>()
    internal val dagpengerRettighetHistorikk = TemporalCollection<Dagpengerettighet>()
    internal val vanligArbeidstidHistorikk = TemporalCollection<Timer>()
    internal val ventetidhistorikk = TemporalCollection<Timer>()
    internal val gjenståendeVentetidHistorikk = TemporalCollection<Timer>()

    init {
        vedtak.forEach { it.populer(this) }
    }

    fun håndter(søknadBehandletHendelse: SøknadBehandletHendelse) {
        val vedtak = søknadBehandletHendelse.tilVedtak()
        leggTilVedtak(vedtak)
    }

    fun håndter(rapporteringsperiode: Rapporteringsperiode) {
        this.leggTilVedtak(
            LøpendeBehandling(
                rapporteringsId = rapporteringsperiode.rapporteringsId,
                satshistorikk = dagsatshistorikk,
                rettighethistorikk = dagpengerRettighetHistorikk,
                vanligarbeidstidhistorikk = vanligArbeidstidHistorikk,
                gjenståendeVentetidhistorikk = gjenståendeVentetidHistorikk,
            ).håndter(rapporteringsperiode),
        )
    }

    fun addObserver(vedtakObserver: VedtakObserver) {
        this.observers.add(vedtakObserver)
    }

    fun accept(visitor: VedtakHistorikkVisitor) {
        if (gjenståendeStønadsperiode.harHistorikk()) {
            visitor.visitGjenståendeStønadsperiode(gjenståendeStønadsperiode.get(LocalDate.now()))
            visitor.visitGjenståendeVentetid(gjenståendeVentetidHistorikk.get(LocalDate.now()))
        }
        visitor.preVisitVedtak()
        vedtak.forEach { it.accept(visitor) }
        visitor.postVisitVedtak()
    }

    fun harVedtak(dato: LocalDate = LocalDate.now()) = vedtak.finn(dato) != null

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
