package no.nav.dagpenger.vedtak.modell.vedtak

import no.nav.dagpenger.vedtak.modell.vedtak.rettighet.Hovedrettighet
import no.nav.dagpenger.vedtak.modell.vedtak.rettighet.Rettighet
import no.nav.dagpenger.vedtak.modell.visitor.VedtakVisitor
import java.time.LocalDate
import java.time.LocalDateTime
import java.util.UUID

class Avslag(
    vedtakId: UUID = UUID.randomUUID(),
    sakId: String,
    behandlingId: UUID,
    vedtakstidspunkt: LocalDateTime,
    virkningsdato: LocalDate,
    private val rettigheter: List<Rettighet>,
) : Vedtak(
        vedtakId = vedtakId,
        sakId = sakId,
        behandlingId = behandlingId,
        vedtakstidspunkt = vedtakstidspunkt,
        virkningsdato = virkningsdato,
        type = VedtakType.Avslag,
    ) {
    companion object {
        fun avslag(
            behandlingId: UUID,
            sakId: String,
            vedtakstidspunkt: LocalDateTime,
            virkningsdato: LocalDate,
            dagpengerettighet: Hovedrettighet,
        ): Avslag {
            return Avslag(
                behandlingId = behandlingId,
                sakId = sakId,
                vedtakstidspunkt = vedtakstidspunkt,
                virkningsdato = virkningsdato,
                rettigheter = listOf(dagpengerettighet),
            )
        }
    }

    override fun accept(visitor: VedtakVisitor) {
        visitor.preVisitVedtak(
            vedtakId = vedtakId,
            sakId = sakId,
            behandlingId = behandlingId,
            virkningsdato = virkningsdato,
            vedtakstidspunkt = vedtakstidspunkt,
            type = type,
        )
        rettigheter.forEach {
            it.accept(visitor)
        }
        visitor.postVisitVedtak(
            vedtakId = vedtakId,
            sakId = sakId,
            behandlingId = behandlingId,
            virkningsdato = virkningsdato,
            vedtakstidspunkt = vedtakstidspunkt,
            type = type,
        )
    }
}
