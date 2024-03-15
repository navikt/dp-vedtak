package no.nav.dagpenger.behandling.modell.hendelser

import no.nav.dagpenger.opplysning.Opplysningstype
import no.nav.dagpenger.opplysning.Regelsett
import java.time.LocalDate
import java.util.UUID

// Baseklasse for alle hendelser som er knyttet til en person som søker om dagpenger
abstract class BehandlingHendelse(
    val meldingsreferanseId: UUID,
    val ident: String,
    val eksternId: ExternId<*>,
    val skjedde: LocalDate,
) : PersonHendelse(meldingsreferanseId, ident) {
    val type: String = this.javaClass.simpleName

    override fun kontekstMap() =
        mapOf(
            "gjelderDato" to skjedde.toString(),
        ) + eksternId.kontekstMap()

    abstract fun regelsett(): List<Regelsett>

    abstract fun avklarer(): Opplysningstype<*>
}
