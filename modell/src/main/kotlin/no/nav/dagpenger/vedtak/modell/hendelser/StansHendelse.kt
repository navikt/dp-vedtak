package no.nav.dagpenger.vedtak.modell.hendelser

import no.nav.dagpenger.aktivitetslogg.Aktivitetslogg
import no.nav.dagpenger.vedtak.modell.vedtak.Stansvedtak
import no.nav.dagpenger.vedtak.modell.vedtak.Vedtak
import java.time.LocalDate
import java.time.LocalDateTime
import java.util.UUID

class StansHendelse(
    meldingsreferanseId: UUID,
    ident: String,
    internal val behandlingId: UUID,
    private val vedtakstidspunkt: LocalDateTime,
    private val virkningsdato: LocalDate,
    aktivitetslogg: Aktivitetslogg = Aktivitetslogg(),
) : Hendelse(meldingsreferanseId, ident, aktivitetslogg) {
    fun tilVedtak(): Vedtak {
        return Stansvedtak(
            behandlingId = behandlingId,
            vedtakstidspunkt = vedtakstidspunkt,
            virkningsdato = virkningsdato,
        )
    }

    override fun kontekstMap(): Map<String, String> = emptyMap()
}
