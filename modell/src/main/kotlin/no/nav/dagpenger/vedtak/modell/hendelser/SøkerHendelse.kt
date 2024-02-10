package no.nav.dagpenger.vedtak.modell.hendelser

import java.time.LocalDate
import java.util.UUID

// Baseklasse for alle hendelser som er knyttet til en person som søker om dagpenger
abstract class SøkerHendelse(
    meldingsreferanseId: UUID,
    val ident: String,
    private val søknadId: UUID,
    internal val gjelderDato: LocalDate,
) : PersonHendelse(meldingsreferanseId, ident) {
    override fun kontekstMap() =
        mapOf(
            // TODO: Dette må fikses
            // søknadId.toString(),
            "søknadId" to UUID.fromString("9d88b4f6-a8c2-4265-a863-6ff7cd181cd8").toString(),
            // gjelderDato.toString(),
            "gjelderDato" to LocalDate.now().toString(),
        )
}
