package no.nav.dagpenger.vedtak.modell.rapportering

import no.nav.dagpenger.vedtak.modell.Dagpengerettighet
import no.nav.dagpenger.vedtak.modell.Person
import no.nav.dagpenger.vedtak.modell.entitet.Beløp.Companion.beløp
import no.nav.dagpenger.vedtak.modell.entitet.Periode
import no.nav.dagpenger.vedtak.modell.entitet.Prosent
import no.nav.dagpenger.vedtak.modell.entitet.Prosent.Companion.summer
import no.nav.dagpenger.vedtak.modell.entitet.Stønadsdager
import no.nav.dagpenger.vedtak.modell.entitet.Timer
import no.nav.dagpenger.vedtak.modell.entitet.Timer.Companion.summer
import no.nav.dagpenger.vedtak.modell.hendelser.Rapporteringshendelse
import no.nav.dagpenger.vedtak.modell.utbetaling.LøpendeRettighetDag.Companion.summer
import no.nav.dagpenger.vedtak.modell.vedtak.Utbetalingsvedtak
import no.nav.dagpenger.vedtak.modell.visitor.PersonVisitor
import java.util.UUID

class Behandling(val behandlingId: UUID, private val person: Person, private var behandlingssteg: Behandlingssteg) {

    constructor(person: Person) : this(UUID.randomUUID(), person, FinnBeregningsgrunnlag)

    private val beregningsgrunnlag = Beregningsgrunnlag()

    private val rapporteringsdager = mutableListOf<Dag>()
    private val rettighetsdagerUtenFravær get() = rapporteringsdager.filter(dagpengerRettighetsdag()).filterNot(fravær())
    private fun fravær(): (Dag) -> Boolean = { it.fravær() }
    private fun dagpengerRettighetsdag(): (Dag) -> Boolean =
        {
            kotlin.runCatching { person.vedtakHistorikk.dagpengerettighetHistorikk.get(it.dato()) }
                .getOrDefault(Dagpengerettighet.Ingen) != Dagpengerettighet.Ingen
        }

    fun håndter(rapporteringshendelse: Rapporteringshendelse) {
        behandlingssteg.håndter(rapporteringshendelse, this)
    }

    private fun nesteSteg(behandlingssteg: Behandlingssteg, rapporteringshendelse: Rapporteringshendelse) {
        this.behandlingssteg = behandlingssteg
        this.behandlingssteg.onEntry(rapporteringshendelse, this)
    }

    sealed class Behandlingssteg {
        // peke til hvilken paragraf eller forskrift
        open fun håndter(rapporteringshendelse: Rapporteringshendelse, behandling: Behandling) {
            rapporteringshendelse.severe("Forventet ikke ${rapporteringshendelse.javaClass.simpleName} i ${this.javaClass.simpleName}")
        }

        open fun onEntry(rapporteringshendelse: Rapporteringshendelse, behandling: Behandling) {}
    }

    object FinnBeregningsgrunnlag : Behandlingssteg() {
        override fun håndter(rapporteringshendelse: Rapporteringshendelse, behandling: Behandling) {
            behandling.rapporteringsdager.addAll(RapporteringsdagerForPeriode(rapporteringshendelse.somPeriode(), behandling.person).dager)
            behandling.beregningsgrunnlag.populer(
                behandling.rapporteringsdager,
                behandling.person.vedtakHistorikk,
            )

            behandling.nesteSteg(VurderTerskelForTaptArbeidstid, rapporteringshendelse)
        }
    }

    object VurderTerskelForTaptArbeidstid : Behandlingssteg() {
        override fun onEntry(rapporteringshendelse: Rapporteringshendelse, behandling: Behandling) {
            val arbeidedeTimer = behandling.rettighetsdagerUtenFravær.map { it.arbeidstimer() }.summer()
            val mandagTilFredagMedRettighet = behandling.beregningsgrunnlag.mandagTilFredagMedRettighet()
            val vanligArbeidstid: Timer = behandling.beregningsgrunnlag.vanligArbeidstid()

            val gjennomsnittsterskel: Prosent = mandagTilFredagMedRettighet.map { it.terskelTaptArbeidstid() }
                .summer() / mandagTilFredagMedRettighet.size.toDouble()
            val minsteTapteArbeidstid: Timer = gjennomsnittsterskel av vanligArbeidstid

            val vilkårForTaptArbeidstidOppfylt = arbeidedeTimer <= vanligArbeidstid - minsteTapteArbeidstid

            if (vilkårForTaptArbeidstidOppfylt) {
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
        override fun onEntry(rapporteringshendelse: Rapporteringshendelse, behandling: Behandling) {
            val rapporteringsperiode = rapporteringshendelse.somPeriode()
            val sisteRapporteringsdato = rapporteringsperiode.endInclusive
            val forrigeRapporteringsdato = rapporteringsperiode.start.minusDays(1)
            val initieltForbruk = behandling.person.vedtakHistorikk.forbrukHistorikk.summer(forrigeRapporteringsdato)
            val stønadsdager = behandling.person.vedtakHistorikk.stønadsdagerHistorikk.get(sisteRapporteringsdato)

            val gjenståendeStønadsperiode = stønadsdager - initieltForbruk
            val arbeidsdagerMedRettighet = behandling.beregningsgrunnlag.mandagTilFredagMedRettighet()

            val antallArbeidsdagerMedRettighet = Stønadsdager(dager = arbeidsdagerMedRettighet.size)
            val arbeidsdagerMedForbruk = if (antallArbeidsdagerMedRettighet > gjenståendeStønadsperiode) {
                arbeidsdagerMedRettighet.subList(0, gjenståendeStønadsperiode.stønadsdager())
            } else {
                arbeidsdagerMedRettighet
            }
            val rettighetsdager =
                arbeidsdagerMedForbruk.map { it.tilLøpendeRettighetDag(behandling.beregningsgrunnlag.graderingsProsent()) }
            val beregnetBeløpFørTrekkAvEgenandel = rettighetsdager.summer()
            val initieltTrukketEgenandel =
                behandling.person.vedtakHistorikk.trukketEgenandelHistorikk.summer(forrigeRapporteringsdato)

            val egenandel = behandling.person.vedtakHistorikk.egenandelHistorikk.get(sisteRapporteringsdato)
            val gjenståendeEgenandel = egenandel - initieltTrukketEgenandel

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
                    forbruk = Stønadsdager(arbeidsdagerMedForbruk.size),
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
}
