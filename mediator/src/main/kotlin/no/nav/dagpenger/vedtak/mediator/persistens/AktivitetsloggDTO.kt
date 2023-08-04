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
        val alvorlighetsgrad: AlvorlighetsgradDTO,
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

    enum class AlvorlighetsgradDTO {
        INFO,
        WARN,
        BEHOV,
        ERROR,
        SEVERE,
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
                when (it.alvorlighetsgrad) {
                    AlvorlighetsgradDTO.INFO -> Aktivitet.Info.gjenopprett(
                        id = it.id,
                        kontekster = kontekster,
                        melding = it.melding,
                        tidsstempel = it.tidsstempel,
                    )
                    AlvorlighetsgradDTO.BEHOV -> Aktivitet.Behov.gjenopprett(
                        id = it.id,
                        kontekster = kontekster,
                        melding = it.melding,
                        tidsstempel = it.tidsstempel,
                        type = behovTypeMapper.map(it.behovtype),
                        detaljer = it.detaljer,
                    )
                    AlvorlighetsgradDTO.SEVERE -> Aktivitet.LogiskFeil.gjenopprett(
                        id = it.id,
                        kontekster = kontekster,
                        melding = it.melding,
                        tidsstempel = it.tidsstempel,
                    )
                    AlvorlighetsgradDTO.WARN -> Aktivitet.FunksjonellFeil.gjennopprett(
                        id = it.id,
                        kontekster = kontekster,
                        melding = it.melding,
                        kode = TODO("Vi har ikke mappe kode enda. "),
                        tidsstempel = it.tidsstempel,
                    )
                    AlvorlighetsgradDTO.ERROR -> Aktivitet.Varsel.gjennopprett(
                        id = it.id,
                        kontekster = kontekster,
                        kode = TODO("Vi har ikke mappe kode enda. "),
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
