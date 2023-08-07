package no.nav.dagpenger.vedtak.iverksetting.hendelser

import no.nav.dagpenger.aktivitetslogg.Aktivitetslogg
import no.nav.dagpenger.vedtak.modell.hendelser.Hendelse
import java.time.LocalDate
import java.time.LocalDateTime
import java.util.UUID

class HovedrettighetVedtakFattetHendelse(
    meldingsreferanseId: UUID,
    ident: String,
    val vedtakId: UUID,
    val behandlingId: UUID,
    val vedtakstidspunkt: LocalDateTime,
    val virkningsdato: LocalDate,
    val utfall: UtbetalingsvedtakFattetHendelse.Utfall,
    aktivitetslogg: Aktivitetslogg = Aktivitetslogg(),
) : Hendelse(meldingsreferanseId, ident, aktivitetslogg) {
    override fun kontekstMap(): Map<String, String> = mapOf("vedtakId" to vedtakId.toString(), "behandlingId" to behandlingId.toString())
}
