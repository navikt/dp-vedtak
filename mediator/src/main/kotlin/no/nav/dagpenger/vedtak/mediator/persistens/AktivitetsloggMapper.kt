package no.nav.dagpenger.vedtak.mediator.persistens

import no.nav.dagpenger.aktivitetslogg.Aktivitet
import no.nav.dagpenger.aktivitetslogg.Aktivitetslogg
import no.nav.dagpenger.aktivitetslogg.AktivitetsloggVisitor
import no.nav.dagpenger.aktivitetslogg.SpesifikkKontekst
import no.nav.dagpenger.aktivitetslogg.Varselkode
import java.util.UUID

class AktivitetsloggMapper(aktivitetslogg: Aktivitetslogg) {

    private enum class AktivitetType {
        INFO,
        BEHOV,
        LOGISK_FEIL,
        FUNKSJONELL_FEIL,
        VARSEL,
    }

    private val aktiviteter = Aktivitetslogginspektør(aktivitetslogg).aktiviteter

    internal fun toMap() = mapOf(
        "aktiviteter" to aktiviteter,
    )

    private inner class Aktivitetslogginspektør(aktivitetslogg: Aktivitetslogg) : AktivitetsloggVisitor {
        val aktiviteter = mutableListOf<Map<String, Any>>()

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
                id = id,
                kontekster = kontekster,
                aktivitetType = AktivitetType.INFO,
                melding = melding,
                tidsstempel = tidsstempel,

            )
        }

        override fun visitLogiskfeil(
            id: UUID,
            kontekster: List<SpesifikkKontekst>,
            aktivitet: Aktivitet.LogiskFeil,
            melding: String,
            tidsstempel: String,
        ) {
            leggTilMelding(
                id = id,
                kontekster = kontekster,
                aktivitetType = AktivitetType.LOGISK_FEIL,
                melding = melding,
                tidsstempel = melding,

            )
        }

        override fun visitFunksjonellFeil(
            id: UUID,
            kontekster: List<SpesifikkKontekst>,
            funksjonellFeil: Aktivitet.FunksjonellFeil,
            melding: String,
            tidsstempel: String,
        ) {
            leggTilMelding(
                id = id,
                kontekster = kontekster,
                aktivitetType = AktivitetType.FUNKSJONELL_FEIL,
                melding = melding,
                tidsstempel = melding,
            )
        }

        override fun visitVarsel(
            id: UUID,
            kontekster: List<SpesifikkKontekst>,
            varsel: Aktivitet.Varsel,
            kode: Varselkode?,
            melding: String,
            tidsstempel: String,
        ) {
            leggTilMelding(
                id = id,
                varselkode = kode,
                kontekster = kontekster,
                aktivitetType = AktivitetType.VARSEL,
                melding = melding,
                tidsstempel = melding,
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

        private fun leggTilMelding(
            id: UUID?,
            varselkode: Varselkode? = null,
            kontekster: List<SpesifikkKontekst>,
            aktivitetType: AktivitetType,
            melding: String,
            tidsstempel: String,
        ) {
            val aktiviteterFraMelding = mutableMapOf<String, Any>(
                "kontekster" to map(kontekster),
                "aktivitetType" to aktivitetType.name,
                "melding" to melding,
                "detaljer" to emptyMap<String, Any>(),
                "tidsstempel" to tidsstempel,
            )
            if (id != null) {
                aktiviteterFraMelding["id"] = id.toString()
            }
            if (varselkode != null) {
                TODO("Støtter ikke varselkode i lagring enda.")
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
