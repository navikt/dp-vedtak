package no.nav.dagpenger.vedtak.modell

import no.nav.dagpenger.aktivitetslogg.Aktivitetskontekst
import no.nav.dagpenger.aktivitetslogg.SpesifikkKontekst
import no.nav.dagpenger.vedtak.modell.Sak.Companion.finnSak
import no.nav.dagpenger.vedtak.modell.entitet.Beløp
import no.nav.dagpenger.vedtak.modell.entitet.Stønadsdager
import no.nav.dagpenger.vedtak.modell.hendelser.Hendelse
import no.nav.dagpenger.vedtak.modell.hendelser.RapporteringHendelse
import no.nav.dagpenger.vedtak.modell.hendelser.RettighetBehandletHendelse
import no.nav.dagpenger.vedtak.modell.hendelser.StansHendelse
import no.nav.dagpenger.vedtak.modell.rapportering.Rapporteringsperiode
import no.nav.dagpenger.vedtak.modell.rapportering.Rapporteringsperioder
import no.nav.dagpenger.vedtak.modell.vedtak.Vedtak
import no.nav.dagpenger.vedtak.modell.vedtak.VedtakHistorikk
import no.nav.dagpenger.vedtak.modell.vedtak.VedtakObserver
import no.nav.dagpenger.vedtak.modell.visitor.PersonVisitor
import java.time.LocalDate

class Person internal constructor(
    private val ident: PersonIdentifikator,
    private val saker: MutableList<Sak>,
    internal val vedtakHistorikk: VedtakHistorikk,
    private val rapporteringsperioder: Rapporteringsperioder,
) : Aktivitetskontekst, VedtakObserver {
    init {
        vedtakHistorikk.addObserver(this)
    }

    constructor(ident: PersonIdentifikator) : this(
        ident = ident,
        saker = mutableListOf(),
        vedtakHistorikk = VedtakHistorikk(),
        rapporteringsperioder = Rapporteringsperioder(),
    )

    companion object {
        val kontekstType: String = "Person"

        fun rehydrer(
            ident: PersonIdentifikator,
            saker: MutableList<Sak>,
            vedtak: List<Vedtak>,
            perioder: List<Rapporteringsperiode>,
        ): Person {
            return Person(
                ident = ident,
                saker = saker,
                vedtakHistorikk = VedtakHistorikk(vedtak.toMutableList()),
                rapporteringsperioder = Rapporteringsperioder(perioder),
            )
        }
    }

    private val observers = mutableListOf<PersonObserver>()

    fun ident() = ident

    fun håndter(rettighetBehandletHendelse: RettighetBehandletHendelse) {
        kontekst(rettighetBehandletHendelse)
        val sak = finnEllerOpprettSak(rettighetBehandletHendelse)
        sak.håndter(rettighetBehandletHendelse)
    }

    private fun finnEllerOpprettSak(rettighetBehandletHendelse: RettighetBehandletHendelse) =
        saker.finnSak(rettighetBehandletHendelse.sakId) ?: Sak(
            rettighetBehandletHendelse.sakId,
            this,
        )

    fun håndter(rapporteringHendelse: RapporteringHendelse) {
        kontekst(rapporteringHendelse)
        rapporteringHendelse.info("Mottatt rapporteringshendelse")
        rapporteringsperioder.håndter(rapporteringHendelse)
        val sak = saker.firstOrNull() ?: rapporteringHendelse.logiskFeil("Vi har ingen sak!")
        sak.håndter(rapporteringHendelse)
    }

    fun håndter(stansHendelse: StansHendelse) {
        kontekst(stansHendelse)
        val sak = saker.firstOrNull() ?: stansHendelse.logiskFeil("Vi har ingen sak!")
        sak.håndter(stansHendelse)
    }

    fun gjenståendeStønadsdagerFra(dato: LocalDate): Stønadsdager = vedtakHistorikk.gjenståendeStønadsdagerFra(dato)

    fun beløpTilUtbetalingFor(dato: LocalDate): Beløp = vedtakHistorikk.beløpTilUtbetalingFor(dato)

    internal fun leggTilVedtak(vedtak: Vedtak) {
        vedtakHistorikk.leggTilVedtak(vedtak)
    }

    override fun vedtakFattet(vedtakFattet: VedtakObserver.VedtakFattet) {
        observers.forEach {
            it.vedtakFattet(ident.identifikator(), vedtakFattet)
        }
    }

    override fun utbetalingsvedtakFattet(utbetalingsvedtakFattet: VedtakObserver.UtbetalingsvedtakFattet) {
        observers.forEach {
            it.utbetalingsvedtakFattet(ident.identifikator(), utbetalingsvedtakFattet)
        }
    }

    fun addObserver(personObserver: PersonObserver) {
        observers.add(personObserver)
    }

    fun accept(visitor: PersonVisitor) {
        visitor.visitPerson(ident)
        saker.forEach { sak ->
            sak.accept(visitor)
        }
        rapporteringsperioder.accept(visitor)
        vedtakHistorikk.accept(visitor)
    }

    internal fun leggTilSak(sak: Sak) {
        saker.add(sak)
    }

    override fun toSpesifikkKontekst(): SpesifikkKontekst = SpesifikkKontekst(kontekstType, mapOf("ident" to ident.identifikator()))

    private fun kontekst(hendelse: Hendelse) {
        hendelse.kontekst(this)
    }
}
