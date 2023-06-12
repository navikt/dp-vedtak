package no.nav.dagpenger.vedtak.modell.hendelser

import no.nav.dagpenger.aktivitetslogg.Aktivitetslogg
import no.nav.dagpenger.vedtak.modell.entitet.Periode
import no.nav.dagpenger.vedtak.modell.entitet.Timer.Companion.timer
import no.nav.dagpenger.vedtak.modell.hendelser.Rapporteringsdag.Aktivitet.Type.Arbeid
import no.nav.dagpenger.vedtak.modell.rapportering.Dag
import no.nav.dagpenger.vedtak.modell.rapportering.Rapporteringsperiode
import java.time.LocalDate
import java.util.UUID
import kotlin.time.Duration

class Rapporteringshendelse(
    ident: String,
    internal val rapporteringsId: UUID,
    rapporteringsdager: List<Rapporteringsdag>,
    aktivitetslogg: Aktivitetslogg = Aktivitetslogg(),
    private val fom: LocalDate,
    private val tom: LocalDate,
) : Hendelse(ident, aktivitetslogg) {
    private val rapporteringsdager = rapporteringsdager.sorted()
    internal fun populerRapporteringsperiode(): Rapporteringsperiode {
        return Rapporteringsperiode(rapporteringsId).also { periode ->
            rapporteringsdager.forEach { rapporteringdag ->
                val dag = when (rapporteringdag.aktiviteter.any { it.type == Arbeid }) {
                    true -> Dag.arbeidsdag(rapporteringdag.dato, rapporteringdag.aktiviteter.first().varighet.timer)
                    false -> Dag.fraværsdag(rapporteringdag.dato)
                }
                periode.leggTilDag(dag)
            }
        }
    }

    internal fun somPeriode() = Periode(fom, tom)
    override fun kontekstMap(): Map<String, String> = mapOf("rapporteringsId" to rapporteringsId.toString())
}

class Rapporteringsdag(val dato: LocalDate, val aktiviteter: List<Rapporteringsdag.Aktivitet>) : Comparable<Rapporteringsdag> {
    override fun compareTo(other: Rapporteringsdag) = this.dato.compareTo(other.dato)

    class Aktivitet(val type: Type, val varighet: Duration) {

        enum class Type {
            Arbeid, Syk, Fravær, Ferie
        }
    }
}
