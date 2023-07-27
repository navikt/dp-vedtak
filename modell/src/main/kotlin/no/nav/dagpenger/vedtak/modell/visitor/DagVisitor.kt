package no.nav.dagpenger.vedtak.modell.visitor

import no.nav.dagpenger.vedtak.modell.rapportering.Aktivitet
import no.nav.dagpenger.vedtak.modell.rapportering.Dag

interface DagVisitor {
    fun visitDag(dag: Dag, aktiviteter: List<Aktivitet>) {}
}
