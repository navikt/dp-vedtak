package no.nav.dagpenger.behandling.modell.hendelser

import no.nav.dagpenger.opplysning.Saksbehandler
import java.time.LocalDateTime
import java.util.UUID

class GodkjennBehandlingHendelse(
    meldingsreferanseId: UUID,
    ident: String,
    override val behandlingId: UUID,
    opprettet: LocalDateTime,
    val godkjentAv: Saksbehandler,
) : PersonHendelse(meldingsreferanseId, ident, opprettet),
    BehandlingHendelse
