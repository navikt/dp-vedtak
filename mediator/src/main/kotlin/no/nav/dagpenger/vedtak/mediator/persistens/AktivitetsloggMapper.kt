package no.nav.dagpenger.vedtak.mediator.persistens

import no.nav.dagpenger.aktivitetslogg.Aktivitet
import no.nav.dagpenger.aktivitetslogg.Aktivitetslogg
import no.nav.dagpenger.aktivitetslogg.AktivitetsloggVisitor
import no.nav.dagpenger.aktivitetslogg.SpesifikkKontekst
import java.util.UUID

class AktivitetsloggMapper(aktivitetslogg: Aktivitetslogg) {

    enum class Alvorlighetsgrad {
        INFO,
        BEHOV,
        SEVERE,
    }

    private val aktiviteter = Aktivitetslogginspektør(aktivitetslogg).aktiviteter

    internal fun toMap() = mutableMapOf(
        "aktiviteter" to aktiviteter,
    )

    private inner class Aktivitetslogginspektør(aktivitetslogg: Aktivitetslogg) : AktivitetsloggVisitor {
        internal val aktiviteter = mutableListOf<Map<String, Any>>()

        init {
            aktivitetslogg.accept(this)
        }

        override fun visitInfo(
            id: UUID,
            kontekster: List<SpesifikkKontekst>,
            aktivitet: Aktivitet.Info,
            melding: String,
            tidsstempel: String,
        ) {
            leggTilMelding(
                id,
                kontekster,
                Alvorlighetsgrad.INFO,
                melding,
                tidsstempel,
            )
        }

        override fun visitBehov(
            id: UUID,
            kontekster: List<SpesifikkKontekst>,
            aktivitet: Aktivitet.Behov,
            type: Aktivitet.Behov.Behovtype,
            melding: String,
            detaljer: Map<String, Any?>,
            tidsstempel: String,
        ) {
        }

        override fun visitSevere(
            kontekster: List<SpesifikkKontekst>,
            severe: Aktivitet.Severe,
            melding: String,
            tidsstempel: String,
        ) {
            leggTilMelding(
                id = null,
                kontekster = listOf(),
                alvorlighetsgrad = Alvorlighetsgrad.SEVERE,
                melding = melding,
                tidsstempel = tidsstempel,
            )
        }

        private fun leggTilMelding(
            id: UUID?,
            kontekster: List<SpesifikkKontekst>,
            alvorlighetsgrad: Alvorlighetsgrad,
            melding: String,
            tidsstempel: String,
        ) {
            val aktiviteterFraMelding = mutableMapOf<String, Any>(
                "kontekster" to map(kontekster),
                "alvorlighetsgrad" to alvorlighetsgrad.name,
                "melding" to melding,
                "detaljer" to emptyMap<String, Any>(),
                "tidsstempel" to tidsstempel,
            )
            if (id != null) {
                aktiviteterFraMelding["id"] = id.toString()
            }

            aktiviteter.add(
                aktiviteterFraMelding,
            )
        }

        private fun map(kontekster: List<SpesifikkKontekst>): List<Map<String, Any>> {
            return kontekster.map {
                mutableMapOf(
                    "kontekstType" to it.kontekstType,
                    "kontekstMap" to it.kontekstMap,
                )
            }
        }
    }
}
