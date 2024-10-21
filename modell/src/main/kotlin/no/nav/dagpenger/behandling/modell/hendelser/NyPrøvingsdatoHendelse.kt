package no.nav.dagpenger.behandling.modell.hendelser

import java.time.LocalDate
import java.time.LocalDateTime
import java.util.UUID

class NyPrøvingsdatoHendelse(
    meldingsreferanseId: UUID,
    ident: String,
    override val behandlingId: UUID,
    val prøvingsdato: LocalDate,
    opprettet: LocalDateTime,
) : PersonHendelse(meldingsreferanseId, ident, opprettet),
    BehandlingHendelse
