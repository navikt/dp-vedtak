package no.nav.dagpenger.vedtak.modell.vedtak

import no.nav.dagpenger.vedtak.modell.vedtak.VedtakObserver.VedtakFattet.Utfall.Avslått
import no.nav.dagpenger.vedtak.modell.vedtak.VedtakObserver.VedtakFattet.Utfall.Innvilget
import no.nav.dagpenger.vedtak.modell.visitor.VedtakVisitor
import java.time.LocalDate
import java.time.LocalDateTime
import java.util.UUID

internal class VedtakFattetVisitor : VedtakVisitor {

    lateinit var vedtakFattet: VedtakObserver.VedtakFattet
    override fun preVisitVedtak(
        vedtakId: UUID,
        behandlingId: UUID,
        virkningsdato: LocalDate,
        vedtakstidspunkt: LocalDateTime,
        utfall: Boolean,
    ) {
        vedtakFattet = VedtakObserver.VedtakFattet(
            vedtakId = vedtakId,
            vedtakstidspunkt = vedtakstidspunkt,
            virkningsdato = virkningsdato,
            utfall = when (utfall) {
                true -> Innvilget
                false -> Avslått
            },

        )
    }
}
