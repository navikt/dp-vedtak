package no.nav.dagpenger.vedtak.mediator.persistens

import no.nav.dagpenger.aktivitetslogg.Aktivitet
import no.nav.dagpenger.aktivitetslogg.Aktivitetslogg
import no.nav.dagpenger.aktivitetslogg.SpesifikkKontekst
import java.util.UUID

data class AktivitetsloggDTO(
    val aktiviteter: List<AktivitetDTO>,
) {
    data class AktivitetDTO(
        val id: UUID,
        val alvorlighetsgrad: Alvorlighetsgrad,
        val label: Char,
        val behovtype: String?,
        val melding: String,
        val tidsstempel: String,
        val kontekster: List<SpesifikkKontekstDTO>,
        val detaljer: Map<String, Any>,
    )

    data class SpesifikkKontekstDTO(
        val kontekstType: String,
        val kontekstMap: Map<String, String>,
    )

    enum class Alvorlighetsgrad {
        INFO,
        BEHOV,

        SEVERE,
    }

    fun konverterTilAktivitetslogg(): Aktivitetslogg = konverterTilAktivitetslogg(this)

    private fun konverterTilAktivitetslogg(aktivitetsloggDTO: AktivitetsloggDTO): Aktivitetslogg {
        val aktiviteter = mutableListOf<Aktivitet>()
        aktivitetsloggDTO.aktiviteter.forEach {
            val kontekster = it.kontekster.map { spesifikkKontekstData ->
                SpesifikkKontekst(
                    spesifikkKontekstData.kontekstType,
                    spesifikkKontekstData.kontekstMap,
                )
            }
            aktiviteter.add(
                when (it.alvorlighetsgrad) {
                    Alvorlighetsgrad.INFO -> Aktivitet.Info.gjenopprett(
                        id = it.id,
                        kontekster = kontekster,
                        melding = it.melding,
                        tidsstempel = it.tidsstempel,
                    )
                    Alvorlighetsgrad.BEHOV -> TODO("Vi har ingen Behov i vedtaksmodellen enda")
                    Alvorlighetsgrad.SEVERE -> Aktivitet.Severe.gjenopprett(
                        id = it.id,
                        kontekster = kontekster,
                        melding = it.melding,
                        tidsstempel = it.tidsstempel,
                    )
                },
            )
        }
        return Aktivitetslogg.rehydrer(
            aktiviteter = aktiviteter,
        )
    }
}
