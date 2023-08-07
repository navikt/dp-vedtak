package no.nav.dagpenger.vedtak.iverksetting.hendelser

import no.nav.dagpenger.aktivitetslogg.Aktivitetslogg
import no.nav.dagpenger.vedtak.modell.hendelser.Hendelse
import java.time.LocalDate
import java.time.LocalDateTime
import java.util.UUID

class UtbetalingsvedtakFattetHendelse(
    meldingsreferanseId: UUID,
    ident: String,
    val vedtakId: UUID,
    val behandlingId: UUID,
    val vedtakstidspunkt: LocalDateTime,
    val virkningsdato: LocalDate,
    val utbetalingsdager: List<Utbetalingsdag>,
    val utfall: Utfall,
    aktivitetslogg: Aktivitetslogg = Aktivitetslogg(),
) :
    Hendelse(meldingsreferanseId, ident, aktivitetslogg) {
    override fun kontekstMap(): Map<String, String> = mapOf("vedtakId" to vedtakId.toString(), "behandlingId" to behandlingId.toString())
    data class Utbetalingsdag(val dato: LocalDate, val beløp: Double)
    enum class Utfall {
        Innvilget,
        Avslått,
    }
}
