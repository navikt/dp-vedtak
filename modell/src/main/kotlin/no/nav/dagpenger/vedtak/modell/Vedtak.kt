package no.nav.dagpenger.vedtak.modell

import no.nav.dagpenger.vedtak.modell.visitor.VedtakVisitor
import java.time.LocalDate
import java.time.LocalDateTime

class Vedtak(
    private val virkningsdato: LocalDate,
    private val beslutningstidspunkt: LocalDateTime,
    private val vedtakId: VedtakIdentifikator,
) {
    fun accept(visitor: VedtakVisitor) {
        visitor.visitVedtak(virkningsdato, beslutningstidspunkt)
    }

    internal fun id() = vedtakId
}
