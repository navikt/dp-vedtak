package no.nav.dagpenger.vedtak.modell

import no.nav.dagpenger.aktivitetslogg.Aktivitetslogg
import no.nav.dagpenger.aktivitetslogg.SpesifikkKontekst
import no.nav.dagpenger.aktivitetslogg.Subaktivitetskontekst
import no.nav.dagpenger.vedtak.modell.entitet.Beløp
import no.nav.dagpenger.vedtak.modell.entitet.Stønadsdager
import no.nav.dagpenger.vedtak.modell.hendelser.Hendelse
import no.nav.dagpenger.vedtak.modell.hendelser.Rapporteringshendelse
import no.nav.dagpenger.vedtak.modell.hendelser.StansHendelse
import no.nav.dagpenger.vedtak.modell.hendelser.SøknadBehandletHendelse
import no.nav.dagpenger.vedtak.modell.rapportering.Behandling
import no.nav.dagpenger.vedtak.modell.rapportering.Rapporteringsperioder
import no.nav.dagpenger.vedtak.modell.vedtak.Vedtak
import no.nav.dagpenger.vedtak.modell.vedtak.Vedtak.Companion.harBehandlet
import no.nav.dagpenger.vedtak.modell.vedtak.VedtakHistorikk
import no.nav.dagpenger.vedtak.modell.vedtak.VedtakObserver
import no.nav.dagpenger.vedtak.modell.visitor.PersonVisitor
import java.time.LocalDate

class Person internal constructor(
    private val ident: PersonIdentifikator,
    internal val vedtakHistorikk: VedtakHistorikk,
    private val rapporteringsperioder: Rapporteringsperioder,
    private val behandlinger: MutableList<Behandling>,
    override val aktivitetslogg: Aktivitetslogg = Aktivitetslogg(),
) : Subaktivitetskontekst, VedtakObserver {

    init {
        vedtakHistorikk.addObserver(this)
    }

    constructor(ident: PersonIdentifikator) : this(
        ident,
        VedtakHistorikk(),
        Rapporteringsperioder(),
        mutableListOf<Behandling>(),
    )

    companion object {
        val kontekstType: String = "Person"

        fun rehydrer(
            ident: PersonIdentifikator,
            vedtak: List<Vedtak>,
        ): Person {
            return Person(ident, VedtakHistorikk(vedtak), Rapporteringsperioder(), mutableListOf())
        }
    }

    private val observers = mutableListOf<PersonObserver>()

    fun ident() = ident

    fun håndter(søknadBehandletHendelse: SøknadBehandletHendelse) {
        kontekst(søknadBehandletHendelse)
        if (vedtakHistorikk.harBehandlet(søknadBehandletHendelse.behandlingId)) {
            søknadBehandletHendelse.info("Har allerede behandlet SøknadBehandletHendelse")
            return
        }
        leggTilVedtak(søknadBehandletHendelse.tilVedtak())
    }

    fun håndter(rapporteringshendelse: Rapporteringshendelse) {
        kontekst(rapporteringshendelse)
        rapporteringsperioder.håndter(rapporteringshendelse)
        val behandling = Behandling(this)
        behandlinger.add(behandling)
        behandling.håndter(rapporteringshendelse)
    }

    fun håndter(stansHendelse: StansHendelse) {
        kontekst(stansHendelse)
        if (vedtakHistorikk.harBehandlet(stansHendelse.behandlingId)) {
            stansHendelse.info("Har allerede behandlet SøknadBehandletHendelse")
            return
        }

        leggTilVedtak(stansHendelse.tilVedtak())
    }

    fun gjenståendeStønadsdagerFra(dato: LocalDate): Stønadsdager = vedtakHistorikk.gjenståendeStønadsdagerFra(dato)

    fun gjenståendeEgenandelFra(dato: LocalDate): Beløp = vedtakHistorikk.gjenståendeEgenandelFra(dato)

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
            it.løpendeVedtakFattet(ident.identifikator(), utbetalingsvedtakFattet)
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

    override fun toSpesifikkKontekst() = SpesifikkKontekst(kontekstType, mapOf("ident" to ident.identifikator()))
}
