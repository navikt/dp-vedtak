package no.nav.dagpenger.behandling.modell

import no.nav.dagpenger.behandling.modell.hendelser.BehandlingHendelse
import no.nav.dagpenger.behandling.modell.hendelser.PersonHendelse
import java.time.LocalDateTime
import java.util.UUID

class BeregningsperiodeHendelse(
    meldingsreferanseId: UUID,
    ident: String,
    override val behandlingId: UUID,
    opprettet: LocalDateTime,
) : PersonHendelse(meldingsreferanseId, ident, opprettet),
    BehandlingHendelse
