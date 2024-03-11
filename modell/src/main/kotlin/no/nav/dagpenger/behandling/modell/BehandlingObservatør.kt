package no.nav.dagpenger.behandling.modell

import no.nav.dagpenger.aktivitetslogg.AktivitetsloggObserver
import no.nav.dagpenger.aktivitetslogg.SpesifikkKontekst
import no.nav.dagpenger.aktivitetslogg.aktivitet.Hendelse
import java.time.LocalDateTime
import java.util.UUID

enum class BehandlingHendelser : Hendelse.Hendelsetype {
    OPPRETTET,
}

interface BehandlingObservatør : AktivitetsloggObserver {
    override fun hendelse(
        id: UUID,
        label: Char,
        type: Hendelse.Hendelsetype,
        melding: String,
        kontekster: List<SpesifikkKontekst>,
        tidsstempel: LocalDateTime,
    ) {
        if (type == BehandlingHendelser.OPPRETTET) {
            behandlingOpprettet(Opprettet(id.toString(), id, kontekster.first().kontekstId)
        }
    }

    fun behandlingOpprettet(behandlingOpprettet: BehandlingEvent.Opprettet) {}

    fun forslagTilVedtak(forslagTilVedtak: BehandlingEvent.ForslagTilVedtak)

    sealed class BehandlingEvent {
        data class Opprettet(
            val ident: String,
            val behandlingId: UUID,
            val søknadId: UUID,
        ) : BehandlingEvent()

        data class ForslagTilVedtak(
            val ident: String,
            val behandlingId: UUID,
            val søknadId: UUID,
        ) : BehandlingEvent()
    }
}
