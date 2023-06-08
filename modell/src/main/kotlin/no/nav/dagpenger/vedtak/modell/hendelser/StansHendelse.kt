package no.nav.dagpenger.vedtak.modell.hendelser

import no.nav.dagpenger.aktivitetslogg.Aktivitetslogg
import no.nav.dagpenger.vedtak.modell.vedtak.Stansvedtak
import no.nav.dagpenger.vedtak.modell.vedtak.Vedtak
import java.time.LocalDate
import java.util.UUID

class StansHendelse(
    ident: String,
    internal val behandlingId: UUID,
    private val virkningsdato: LocalDate,
    aktivitetslogg: Aktivitetslogg = Aktivitetslogg(),
) : Hendelse(ident, aktivitetslogg) {
    fun tilVedtak(): Vedtak {
        return Stansvedtak(
            behandlingId = behandlingId,
            virkningsdato = virkningsdato,
        )
    }

    override fun kontekstMap(): Map<String, String> = emptyMap()
}
