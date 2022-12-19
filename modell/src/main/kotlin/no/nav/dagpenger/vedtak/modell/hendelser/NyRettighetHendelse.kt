package no.nav.dagpenger.vedtak.modell.hendelser

import no.nav.dagpenger.vedtak.kontomodell.mengder.RatioMengde
import no.nav.dagpenger.vedtak.modell.Beløp
import java.time.LocalDate
import java.time.LocalDateTime
import java.util.UUID

sealed class RettighetHendelse(
    internal val behandlingsId: UUID, // behandlingen inneholder rettighetstypen?
    internal val virkningsdato: LocalDate,
    internal val beslutningstidspunkt: LocalDateTime,
    internal val dagsats: Beløp,
    internal val dagpengerPeriode: RatioMengde,
    internal val ventedager: RatioMengde
)

class Ordinær(
    behandlingsId: UUID,
    virkningsdato: LocalDate,
    beslutningstidspunkt: LocalDateTime,
    dagsats: Beløp,
    dagpengerPeriode: RatioMengde,
    ventedager: RatioMengde
) : RettighetHendelse(
    behandlingsId = behandlingsId,
    virkningsdato = virkningsdato,
    beslutningstidspunkt = beslutningstidspunkt,
    dagsats = dagsats,
    dagpengerPeriode = dagpengerPeriode,
    ventedager = ventedager
)

class Permittering(
    behandlingsId: UUID,
    virkningsdato: LocalDate,
    beslutningstidspunkt: LocalDateTime,
    dagsats: Beløp,
    dagpengerPeriode: RatioMengde,
    ventedager: RatioMengde
) :
    RettighetHendelse(
        behandlingsId = behandlingsId,
        virkningsdato = virkningsdato,
        beslutningstidspunkt = beslutningstidspunkt,
        dagsats = dagsats,
        dagpengerPeriode = dagpengerPeriode,
        ventedager = ventedager
    )


