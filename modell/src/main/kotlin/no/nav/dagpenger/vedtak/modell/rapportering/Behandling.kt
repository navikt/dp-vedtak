package no.nav.dagpenger.vedtak.modell.rapportering

import no.nav.dagpenger.aktivitetslogg.Aktivitetskontekst
import no.nav.dagpenger.aktivitetslogg.SpesifikkKontekst
import no.nav.dagpenger.vedtak.modell.Dagpengerettighet
import no.nav.dagpenger.vedtak.modell.Person
import no.nav.dagpenger.vedtak.modell.entitet.Beløp.Companion.beløp
import no.nav.dagpenger.vedtak.modell.entitet.Periode
import no.nav.dagpenger.vedtak.modell.entitet.Prosent
import no.nav.dagpenger.vedtak.modell.entitet.Prosent.Companion.summer
import no.nav.dagpenger.vedtak.modell.entitet.Stønadsdager
import no.nav.dagpenger.vedtak.modell.entitet.Timer
import no.nav.dagpenger.vedtak.modell.entitet.Timer.Companion.summer
import no.nav.dagpenger.vedtak.modell.entitet.Timer.Companion.timer
import no.nav.dagpenger.vedtak.modell.hendelser.Rapporteringshendelse
import no.nav.dagpenger.vedtak.modell.utbetaling.BeregnetBeløpDag
import no.nav.dagpenger.vedtak.modell.utbetaling.LøpendeRettighetDag.Companion.summer
import no.nav.dagpenger.vedtak.modell.vedtak.Utbetalingsvedtak
import no.nav.dagpenger.vedtak.modell.visitor.PersonVisitor
import java.util.UUID

class Behandling(val behandlingId: UUID, private val person: Person, private var behandlingssteg: Behandlingssteg) :
    Aktivitetskontekst {

    constructor(person: Person) : this(UUID.randomUUID(), person, FinnBeregningsgrunnlag)

    private val rapporteringsdager = mutableListOf<Dag>()
    private val rettighetsdagerUtenFravær
        get() = rapporteringsdager.filter(dagpengerRettighetsdag()).filterNot(fravær())
    private val tellendeRapporteringsdager get() = rettighetsdagerUtenFravær.filterNot(helgedag())
    private val vanligArbeidstid
        get() = tellendeRapporteringsdager.map {
            vanligArbeidstid(it)
        }.summer()

    private fun vanligArbeidstid(it: Dag) =
        kotlin.runCatching { person.vedtakHistorikk.vanligArbeidstidHistorikk.get(it.dato()) }
            .getOrDefault(0.timer)

    private val arbeidedeTimer get() = rettighetsdagerUtenFravær.map { it.arbeidstimer() }.summer()
    private val taptArbeidstid get() = (vanligArbeidstid - arbeidedeTimer)
    private val graderingsProsent get() = taptArbeidstid prosentAv vanligArbeidstid

    private fun helgedag(): (Dag) -> Boolean = { it.erHelg() }
    private fun fravær(): (Dag) -> Boolean = { it.fravær() }
    private fun dagpengerRettighetsdag(): (Dag) -> Boolean =
        {
            dagpengerettighet(it) != Dagpengerettighet.Ingen
        }

    private fun dagpengerettighet(dag: Dag) =
        kotlin.runCatching { person.vedtakHistorikk.dagpengerettighetHistorikk.get(dag.dato()) }
            .getOrDefault(Dagpengerettighet.Ingen)

    fun håndter(rapporteringshendelse: Rapporteringshendelse) {
        rapporteringshendelse.kontekst(this)
        behandlingssteg.håndter(rapporteringshendelse, this)
    }

    private fun nesteSteg(behandlingssteg: Behandlingssteg, rapporteringshendelse: Rapporteringshendelse) {
        this.behandlingssteg = behandlingssteg
        this.behandlingssteg.entering(rapporteringshendelse, this)
    }

    sealed class Behandlingssteg {
        // peke til hvilken paragraf eller forskrift
        open fun håndter(rapporteringshendelse: Rapporteringshendelse, behandling: Behandling) {
            rapporteringshendelse.severe("Forventet ikke ${rapporteringshendelse.javaClass.simpleName} i ${this.javaClass.simpleName}")
        }

        open fun entering(rapporteringshendelse: Rapporteringshendelse, behandling: Behandling) {}
    }

    object FinnBeregningsgrunnlag : Behandlingssteg() {
        override fun håndter(rapporteringshendelse: Rapporteringshendelse, behandling: Behandling) {
            behandling.rapporteringsdager.addAll(
                RapporteringsdagerForPeriode(
                    rapporteringshendelse.somPeriode(),
                    behandling.person,
                ).dager,
            )

            behandling.nesteSteg(VurderTerskelForTaptArbeidstid, rapporteringshendelse)
        }
    }

    object VurderTerskelForTaptArbeidstid : Behandlingssteg() {
        override fun entering(rapporteringshendelse: Rapporteringshendelse, behandling: Behandling) {
            val arbeidedeTimer = behandling.rettighetsdagerUtenFravær.map { it.arbeidstimer() }.summer()

            val terskelProsent: Prosent = behandling.tellendeRapporteringsdager.map {
                TaptArbeidstidTerskel.terskelFor(behandling.dagpengerettighet(it), it.dato())
            }.summer() / behandling.tellendeRapporteringsdager.size.toDouble()

            val terskelTimer: Timer = terskelProsent av behandling.vanligArbeidstid

            val vilkårOppfylt = arbeidedeTimer <= behandling.vanligArbeidstid - terskelTimer

            if (vilkårOppfylt) {
                behandling.nesteSteg(GraderUtbetaling, rapporteringshendelse)
            } else {
                behandling.person.leggTilVedtak(
                    Utbetalingsvedtak.utbetalingsvedtak(
                        behandlingId = UUID.randomUUID(),
                        utfall = false,
                        virkningsdato = rapporteringshendelse.somPeriode().endInclusive,
                    ),
                )
            }
        }
    }

    object GraderUtbetaling : Behandlingssteg() {
        override fun entering(rapporteringshendelse: Rapporteringshendelse, behandling: Behandling) {
            val rapporteringsperiode = rapporteringshendelse.somPeriode()
            val rapporteringsdato = rapporteringsperiode.endInclusive

            val forrigeRapporteringsdato = rapporteringsperiode.start.minusDays(1)
            val forrigeAkkumulerteForbruk = behandling.person.vedtakHistorikk.forbrukHistorikk.summer(forrigeRapporteringsdato)
            val innvilgedeStønadsdager = behandling.person.vedtakHistorikk.stønadsdagerHistorikk.get(rapporteringsdato)

            val gjenståendeStønadsperiode = innvilgedeStønadsdager - forrigeAkkumulerteForbruk

            val forbruksdager = if (Stønadsdager(dager = behandling.tellendeRapporteringsdager.size) > gjenståendeStønadsperiode) {
                behandling.tellendeRapporteringsdager.subList(0, gjenståendeStønadsperiode.stønadsdager())
            } else {
                behandling.tellendeRapporteringsdager
            }

            val rettighetsdager =
                forbruksdager.map {
                    BeregnetBeløpDag(
                        it.dato(),
                        behandling.graderingsProsent * behandling.person.vedtakHistorikk.dagsatsHistorikk.get(it.dato()),
                    )
                }

            val beregnetBeløpFørTrekkAvEgenandel = rettighetsdager.summer()
            val forrigeTrukketEgenandel =
                behandling.person.vedtakHistorikk.trukketEgenandelHistorikk.summer(forrigeRapporteringsdato)

            val egenandel = behandling.person.vedtakHistorikk.egenandelHistorikk.get(rapporteringsdato)
            val gjenståendeEgenandel = egenandel - forrigeTrukketEgenandel
            val trukketEgenandel = if (gjenståendeEgenandel > 0.beløp && beregnetBeløpFørTrekkAvEgenandel > 0.beløp) {
                minOf(gjenståendeEgenandel, beregnetBeløpFørTrekkAvEgenandel)
            } else {
                0.beløp
            }

            behandling.person.leggTilVedtak(
                Utbetalingsvedtak.utbetalingsvedtak(
                    behandlingId = behandling.behandlingId,
                    utfall = true,
                    virkningsdato = rapporteringsperiode.endInclusive,
                    forbruk = Stønadsdager(forbruksdager.size),
                    rettighetsdager = rettighetsdager,
                    trukketEgenandel = trukketEgenandel,
                ),
            )
        }
    }

    private class RapporteringsdagerForPeriode(private val periode: Periode, person: Person) : PersonVisitor {

        val dager = mutableListOf<Dag>()

        init {
            person.accept(this)
        }

        override fun visitdag(dag: Dag) {
            if (dag.dato() in periode) {
                dager.add(dag)
            }
        }
    }

    override fun toSpesifikkKontekst(): SpesifikkKontekst {
        return SpesifikkKontekst("behandling", mapOf("behandlingId" to behandlingId.toString()))
    }
}
