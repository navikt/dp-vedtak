package no.nav.dagpenger.vedtak.modell.vedtak

import no.nav.dagpenger.vedtak.modell.visitor.VedtakVisitor
import java.time.LocalDate
import java.time.LocalDateTime
import java.util.UUID

class Stansvedtak(
    vedtakId: UUID = UUID.randomUUID(),
    sakId: String,
    behandlingId: UUID,
    vedtakstidspunkt: LocalDateTime,
    virkningsdato: LocalDate,
    private val utfall: Boolean = false,
) : Vedtak(vedtakId, sakId, behandlingId, vedtakstidspunkt, virkningsdato, VedtakType.Stans) {
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
