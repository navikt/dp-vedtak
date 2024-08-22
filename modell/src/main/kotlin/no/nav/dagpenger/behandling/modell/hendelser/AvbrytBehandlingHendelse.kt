package no.nav.dagpenger.behandling.modell.hendelser

import java.time.LocalDateTime
import java.util.UUID

class AvbrytBehandlingHendelse(
    meldingsreferanseId: UUID,
    ident: String,
    override val behandlingId: UUID,
    val årsak: String,
    opprettet: LocalDateTime,
) : PersonHendelse(meldingsreferanseId, ident, opprettet),
    BehandlingHendelse
