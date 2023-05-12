package no.nav.dagpenger.vedtak.modell.hendelser

import no.nav.dagpenger.vedtak.modell.Aktivitetslogg
import no.nav.dagpenger.vedtak.modell.entitet.Periode
import no.nav.dagpenger.vedtak.modell.entitet.Timer.Companion.timer
import no.nav.dagpenger.vedtak.modell.rapportering.Dag
import no.nav.dagpenger.vedtak.modell.rapportering.Rapporteringsperiode
import java.time.LocalDate
import java.util.UUID

class Rapporteringshendelse(
    ident: String,
    internal val rapporteringsId: UUID,
    rapporteringsdager: List<Rapporteringsdag>,
    aktivitetslogg: Aktivitetslogg = Aktivitetslogg(),
) : Hendelse(ident, aktivitetslogg) {
    private val rapporteringsdager = rapporteringsdager.sorted()
    internal fun populerRapporteringsperiode(): Rapporteringsperiode {
        return Rapporteringsperiode(rapporteringsId).also { periode ->
            rapporteringsdager.forEach {
                val dag = when (it.fravær) {
                    true -> Dag.fraværsdag(it.dato)
                    false -> Dag.arbeidsdag(it.dato, it.timer.timer)
                }
                periode.leggTilDag(dag)
            }
        }
    }

    internal fun somPeriode() = Periode(rapporteringsdager.first().dato, rapporteringsdager.last().dato)
    override fun kontekstMap(): Map<String, String> = mapOf("rapporteringsId" to rapporteringsId.toString())
}

class Rapporteringsdag(val dato: LocalDate, val fravær: Boolean, val timer: Number = 0) : Comparable<Rapporteringsdag> {
    override fun compareTo(other: Rapporteringsdag) = this.dato.compareTo(other.dato)
}
