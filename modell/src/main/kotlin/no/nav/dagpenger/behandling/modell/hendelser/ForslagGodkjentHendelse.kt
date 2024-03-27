package no.nav.dagpenger.behandling.modell.hendelser

import java.util.UUID

class ForslagGodkjentHendelse(
    meldingsreferanseId: UUID,
    ident: String,
    override val behandlingId: UUID,
) : PersonHendelse(meldingsreferanseId, ident), BehandlingHendelse
