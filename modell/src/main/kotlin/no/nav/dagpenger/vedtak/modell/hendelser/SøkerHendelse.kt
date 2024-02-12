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
            "søknadId" to søknadId.toString(),
            "gjelderDato" to gjelderDato.toString(),
        )
}
