package no.nav.dagpenger.vedtak.modell.hendelser

import java.time.LocalDate
import java.util.UUID

class SøknadInnsendtHendelse(
    meldingsreferanseId: UUID,
    ident: String,
    søknadId: UUID,
    gjelderDato: LocalDate,
) : SøkerHendelse(meldingsreferanseId, ident, søknadId, gjelderDato)
