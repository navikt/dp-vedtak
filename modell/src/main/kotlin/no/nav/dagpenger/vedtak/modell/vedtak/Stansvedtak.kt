package no.nav.dagpenger.vedtak.modell.vedtak

import no.nav.dagpenger.vedtak.modell.visitor.VedtakVisitor
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.temporal.ChronoUnit
import java.util.UUID

class Stansvedtak(
    vedtakId: UUID = UUID.randomUUID(),
    behandlingId: UUID,
    vedtakstidspunkt: LocalDateTime = LocalDateTime.now().truncatedTo(ChronoUnit.SECONDS),
    virkningsdato: LocalDate,
    private val utfall: Boolean = false,
) : Vedtak(vedtakId, behandlingId, vedtakstidspunkt, virkningsdato, VedtakType.Stans) {
    override fun accept(visitor: VedtakVisitor) {
        visitor.visitStans(
            vedtakId,
            behandlingId,
            virkningsdato,
            vedtakstidspunkt,
            utfall,
        )
    }
}
