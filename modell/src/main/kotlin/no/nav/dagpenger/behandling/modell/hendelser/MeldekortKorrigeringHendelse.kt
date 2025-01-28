package no.nav.dagpenger.behandling.modell.hendelser

import java.time.LocalDate
import java.time.LocalDateTime
import java.util.UUID

// Skulle vi ha arvet fra MeldekortHendelse her?
class MeldekortKorrigeringHendelse(
    meldingsreferanseId: UUID,
    ident: String,
    val meldekortId: Long,
    val fom: LocalDate,
    val tom: LocalDate,
    val kilde: MeldekortKilde,
    val dager: List<Dag>,
    val orginalMeldekortId: Long,
    opprettet: LocalDateTime,
) : PersonHendelse(meldingsreferanseId, ident, opprettet)
