package no.nav.dagpenger.vedtak.modell.rapportering

import no.nav.dagpenger.vedtak.modell.Person
import no.nav.dagpenger.vedtak.modell.entitet.Periode
import no.nav.dagpenger.vedtak.modell.entitet.Prosent
import no.nav.dagpenger.vedtak.modell.entitet.Prosent.Companion.summer
import no.nav.dagpenger.vedtak.modell.entitet.Timer
import no.nav.dagpenger.vedtak.modell.hendelser.Rapporteringshendelse
import no.nav.dagpenger.vedtak.modell.vedtak.Utbetalingsvedtak
import no.nav.dagpenger.vedtak.modell.visitor.PersonVisitor
import java.util.UUID

class Behandling(behandlingId: UUID, private val person: Person, private var behandlingssteg: Behandlingssteg) {

    constructor(person: Person) : this(UUID.randomUUID(), person, FinnBeregningsgrunnlag)

    private val beregningsgrunnlag = Beregningsgrunnlag()

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
            val rapporteringsdager = RapporteringsdagerForPeriode(rapporteringshendelse.somPeriode(), behandling.person).dager
            behandling.beregningsgrunnlag.populer(
                rapporteringsdager,
                behandling.person.vedtakHistorikk,
            )

            behandling.nesteSteg(VurderTerskelForTaptArbeidstid, rapporteringshendelse)
        }
    }

    object VurderTerskelForTaptArbeidstid : Behandlingssteg() {
        override fun onEntry(rapporteringshendelse: Rapporteringshendelse, behandling: Behandling) {
            val arbeidedeTimer = behandling.beregningsgrunnlag.arbeidedeTimer()
            val mandagTilFredagMedRettighet = behandling.beregningsgrunnlag.mandagTilFredagMedRettighet()
            val vanligArbeidstid: Timer = behandling.beregningsgrunnlag.vanligArbeidstid()

            val gjennomsnittsterskel: Prosent = mandagTilFredagMedRettighet.map { it.terskelTaptArbeidstid() }.summer() / mandagTilFredagMedRettighet.size.toDouble()
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

    object GraderUtbetaling : Behandlingssteg()

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
