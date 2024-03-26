package no.nav.dagpenger.behandling.modell.hendelser

import java.util.UUID

class AvbrytBehandlingHendelse(
    meldingsreferanseId: UUID,
    ident: String,
    val behandlingId: UUID,
) : PersonHendelse(meldingsreferanseId, ident)
