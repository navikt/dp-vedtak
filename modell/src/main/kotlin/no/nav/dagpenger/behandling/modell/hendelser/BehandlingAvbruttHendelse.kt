package no.nav.dagpenger.behandling.modell.hendelser

import java.util.UUID

class BehandlingAvbruttHendelse(
    meldingsreferanseId: UUID,
    ident: String,
) : PersonHendelse(meldingsreferanseId, ident)
