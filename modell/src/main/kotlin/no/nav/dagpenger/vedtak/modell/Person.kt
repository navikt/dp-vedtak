package no.nav.dagpenger.vedtak.modell

import no.nav.dagpenger.aktivitetslogg.Aktivitetskontekst
import no.nav.dagpenger.aktivitetslogg.Aktivitetslogg
import no.nav.dagpenger.vedtak.modell.entitet.Beløp
import no.nav.dagpenger.vedtak.modell.entitet.Stønadsdager
import no.nav.dagpenger.vedtak.modell.hendelser.Hendelse
import no.nav.dagpenger.vedtak.modell.hendelser.Rapporteringshendelse
import no.nav.dagpenger.vedtak.modell.hendelser.StansHendelse
import no.nav.dagpenger.vedtak.modell.hendelser.SøknadBehandletHendelse
import no.nav.dagpenger.vedtak.modell.rapportering.Rapporteringsperioder
import no.nav.dagpenger.vedtak.modell.vedtak.VedtakHistorikk
import no.nav.dagpenger.vedtak.modell.vedtak.VedtakObserver
import no.nav.dagpenger.vedtak.modell.visitor.PersonVisitor
import java.time.LocalDate

class Person private constructor(
    private val ident: PersonIdentifikator,
    private val vedtakHistorikk: VedtakHistorikk,
    private val rapporteringsperioder: Rapporteringsperioder,
    internal val aktivitetslogg: Aktivitetslogg = Aktivitetslogg(),
) : Aktivitetskontekst by ident, VedtakObserver {

    init {
        vedtakHistorikk.addObserver(this)
    }

    constructor(ident: PersonIdentifikator) : this(ident, VedtakHistorikk(), Rapporteringsperioder())

    private val observers = mutableListOf<PersonObserver>()

    fun ident() = ident
    fun håndter(søknadBehandletHendelse: SøknadBehandletHendelse) {
        kontekst(søknadBehandletHendelse)
        vedtakHistorikk.håndter(søknadBehandletHendelse)
    }

    fun håndter(rapporteringshendelse: Rapporteringshendelse) {
        kontekst(rapporteringshendelse)
        val rapporteringsperiode = rapporteringsperioder.håndter(rapporteringshendelse)
        // @todo: En burde håndtere hendelsen inn i vedtakhistorikk for sporing
        vedtakHistorikk.håndter(rapporteringsperiode)
    }

    fun håndter(stansHendelse: StansHendelse) {
        kontekst(stansHendelse)
        vedtakHistorikk.håndter(stansHendelse)
    }

    fun gjenståendeStønadsdagerFra(dato: LocalDate): Stønadsdager = vedtakHistorikk.gjenståendeStønadsdagerFra(dato)

    fun gjenståendeEgenandelFra(dato: LocalDate): Beløp = vedtakHistorikk.gjenståendeEgenandelFra(dato)

    fun beløpTilUtbetalingFor(dato: LocalDate): Beløp = vedtakHistorikk.beløpTilUtbetalingFor(dato)

    override fun rammevedtakFattet(rammevedtakFattet: VedtakObserver.RammevedtakFattet) {
        observers.forEach {
            it.rammevedtakFattet(ident.identifikator(), rammevedtakFattet)
        }
    }

    override fun løpendeVedtakFattet(løpendeVedtakFattet: VedtakObserver.LøpendeVedtakFattet) {
        observers.forEach {
            it.løpendeVedtakFattet(ident.identifikator(), løpendeVedtakFattet)
        }
    }

    fun addObserver(personObserver: PersonObserver) {
        observers.add(personObserver)
    }

    fun accept(visitor: PersonVisitor) {
        visitor.visitPerson(ident)
        aktivitetslogg.accept(visitor)
        rapporteringsperioder.accept(visitor)
        vedtakHistorikk.accept(visitor)
    }

    private fun kontekst(hendelse: Hendelse) {
        hendelse.kontekst(this)
    }

    companion object {
        val kontekstType: String = "Person"
    }
}
