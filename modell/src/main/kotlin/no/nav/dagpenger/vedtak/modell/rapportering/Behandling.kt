package no.nav.dagpenger.vedtak.modell.rapportering

import no.nav.dagpenger.vedtak.modell.Person
import no.nav.dagpenger.vedtak.modell.entitet.Periode
import no.nav.dagpenger.vedtak.modell.hendelser.Rapporteringshendelse
import no.nav.dagpenger.vedtak.modell.visitor.PersonVisitor
import java.util.UUID

class Behandling(behandlingId: UUID, private val person: Person, private val behandlingssteg: Behandlingssteg) {

    constructor(person: Person) : this(UUID.randomUUID(), person, FinnBeregningsgrunnlag)

    private val beregningsgrunnlag = Beregningsgrunnlag()

    fun håndter(rapporteringshendelse: Rapporteringshendelse) {
        behandlingssteg.håndter(rapporteringshendelse, this)
    }

    sealed class Behandlingssteg {
        // peke til hvilken paragraf eller forskrift
        open fun håndter(rapporteringshendelse: Rapporteringshendelse, behandling: Behandling) {
            rapporteringshendelse.severe("Forventet ikke ${rapporteringshendelse.javaClass.simpleName} i ${this.javaClass.simpleName}")
        }
    }

    object FinnBeregningsgrunnlag : Behandlingssteg() {
        override fun håndter(rapporteringshendelse: Rapporteringshendelse, behandling: Behandling) {
            val rapporteringsdager = RapporteringsdagerForPeriode(rapporteringshendelse.somPeriode(), behandling.person).dager
            behandling.beregningsgrunnlag.populer(
                rapporteringsdager,
                behandling.person.vedtakHistorikk,
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
