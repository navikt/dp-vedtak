package no.nav.dagpenger.behandling.modell.hendelser

import java.util.UUID

class ManuellBehandlingAvklartHendelse(
    meldingsreferanseId: UUID,
    ident: String,
    val behandlingId: UUID,
    val behandlesManuelt: Boolean,
) : PersonHendelse(meldingsreferanseId, ident)
