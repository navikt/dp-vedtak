package no.nav.dagpenger.vedtak.modell.vedtak

import no.nav.dagpenger.vedtak.modell.Dagpengerettighet
import no.nav.dagpenger.vedtak.modell.vedtak.rettighet.Ordinær
import no.nav.dagpenger.vedtak.modell.vedtak.rettighet.Permittering
import no.nav.dagpenger.vedtak.modell.vedtak.rettighet.PermitteringFraFiskeindustrien
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
            dagpengerettighet: Dagpengerettighet,
        ): Avslag {
            val rettighet = when (dagpengerettighet) {
                Dagpengerettighet.Ordinær -> Ordinær(utfall = false)
                Dagpengerettighet.Permittering -> Permittering(utfall = false)
                Dagpengerettighet.PermitteringFraFiskeindustrien -> PermitteringFraFiskeindustrien(utfall = false)
                Dagpengerettighet.ForskutterteLønnsgarantimidler -> TODO()
                Dagpengerettighet.Ingen -> TODO()
            }
            return Avslag(
                behandlingId = behandlingId,
                sakId = sakId,
                vedtakstidspunkt = vedtakstidspunkt,
                virkningsdato = virkningsdato,
                rettigheter = listOf(rettighet),
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
