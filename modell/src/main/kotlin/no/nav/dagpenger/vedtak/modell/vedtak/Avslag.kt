package no.nav.dagpenger.vedtak.modell.vedtak

import no.nav.dagpenger.vedtak.modell.visitor.VedtakVisitor
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.temporal.ChronoUnit
import java.util.UUID

class Avslag private constructor(
    vedtakId: UUID,
    behandlingId: UUID,
    vedtakstidspunkt: LocalDateTime,
    virkningsdato: LocalDate,
    private val utfall: Boolean = false,
) : Vedtak(
    vedtakId = vedtakId,
    behandlingId = behandlingId,
    vedtakstidspunkt = vedtakstidspunkt,
    virkningsdato = virkningsdato,
    type = VedtakType.Avslag,
) {

    companion object {
        fun avslag(behandlingId: UUID, virkningsdato: LocalDate) =
            Avslag(behandlingId = behandlingId, virkningsdato = virkningsdato)
    }
    constructor(behandlingId: UUID, virkningsdato: LocalDate) : this(
        vedtakId = UUID.randomUUID(),
        behandlingId = behandlingId,
        vedtakstidspunkt = LocalDateTime.now().truncatedTo(ChronoUnit.SECONDS),
        virkningsdato = virkningsdato,
    )

    override fun accept(visitor: VedtakVisitor) {
        visitor.visitAvslag(
            vedtakId = vedtakId,
            behandlingId = behandlingId,
            vedtakstidspunkt = vedtakstidspunkt,
            utfall = utfall,
            virkningsdato = virkningsdato,
        )
    }
}
