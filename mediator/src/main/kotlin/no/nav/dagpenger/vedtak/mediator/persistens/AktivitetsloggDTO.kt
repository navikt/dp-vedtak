package no.nav.dagpenger.vedtak.mediator.persistens

import no.nav.dagpenger.aktivitetslogg.Aktivitet
import no.nav.dagpenger.aktivitetslogg.Aktivitetslogg
import no.nav.dagpenger.aktivitetslogg.SpesifikkKontekst
import java.util.UUID

fun interface BehovTypeMapper {
    fun map(behovNavn: String?): Aktivitet.Behov.Behovtype
}

private object NoOpBehovtypeMapper : BehovTypeMapper {
    override fun map(behovNavn: String?): Aktivitet.Behov.Behovtype {
        throw RuntimeException("Implementer egen BehovTypeMapper")
    }
}

data class AktivitetsloggDTO(
    val aktiviteter: List<AktivitetDTO>,
) {
    data class AktivitetDTO(
        val id: UUID,
        val aktivitetType: AktivitetType,
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

    enum class AktivitetType {
        INFO,
        BEHOV,
        LOGISK_FEIL,
        FUNKSJONELL_FEIL,
        VARSEL,
    }

    fun konverterTilAktivitetslogg(behovTypeMapper: BehovTypeMapper = NoOpBehovtypeMapper): Aktivitetslogg = konverterTilAktivitetslogg(this, behovTypeMapper)

    private fun konverterTilAktivitetslogg(aktivitetsloggDTO: AktivitetsloggDTO, behovTypeMapper: BehovTypeMapper): Aktivitetslogg {
        val aktiviteter = mutableListOf<Aktivitet>()
        aktivitetsloggDTO.aktiviteter.forEach {
            val kontekster = it.kontekster.map { spesifikkKontekstData ->
                SpesifikkKontekst(
                    spesifikkKontekstData.kontekstType,
                    spesifikkKontekstData.kontekstMap,
                )
            }
            aktiviteter.add(
                when (it.aktivitetType) {
                    AktivitetType.INFO -> Aktivitet.Info.gjenopprett(
                        id = it.id,
                        kontekster = kontekster,
                        melding = it.melding,
                        tidsstempel = it.tidsstempel,
                    )
                    AktivitetType.BEHOV -> Aktivitet.Behov.gjenopprett(
                        id = it.id,
                        kontekster = kontekster,
                        melding = it.melding,
                        tidsstempel = it.tidsstempel,
                        type = behovTypeMapper.map(it.behovtype),
                        detaljer = it.detaljer,
                    )
                    else -> TODO("TODO!!! Har ikke mappet ${it.aktivitetType} fra databasen enda.")
                },
            )
        }
        return Aktivitetslogg.rehydrer(
            aktiviteter = aktiviteter,
        )
    }
}
