package no.nav.dagpenger.behandling.modell

import no.nav.dagpenger.aktivitetslogg.AktivitetsloggObserver
import no.nav.dagpenger.aktivitetslogg.SpesifikkKontekst
import no.nav.dagpenger.aktivitetslogg.aktivitet.Hendelse
import java.time.LocalDateTime
import java.util.UUID

interface BehandlingObservatørAdapter : AktivitetsloggObserver, BehandlingObservatør {
    override fun hendelse(
        id: UUID,
        label: Char,
        type: Hendelse.Hendelsetype,
        melding: String,
        kontekster: List<SpesifikkKontekst>,
        tidsstempel: LocalDateTime,
    ) {
        if (type !is BehandlingHendelser) return

        // TODO: Nå er testene helt avhengige av hele aggratmodellen og hiearkiet
        // val person = kontekster.filterIsInstance<Person.PersonKontekst>().single()
        // val behandling = kontekster.filterIsInstance<Behandling.BehandlingKontekst>().single()

        val kontekstMap = kontekster.flatMap { it.kontekstMap.entries }
        val ident = kontekstMap.first { it.key == "ident" }.value
        val behandlingId = UUID.fromString(kontekstMap.first { it.key == "behandlingId" }.value)
        val søknadId = UUID.fromString(kontekstMap.first { it.key == "søknadId" }.value)

        when (type) {
            BehandlingHendelser.behandling_opprettet ->
                behandlingOpprettet(
                    BehandlingObservatør.BehandlingEvent.Opprettet(
                        ident,
                        behandlingId,
                        søknadId,
                    ),
                )

            BehandlingHendelser.forslag_til_vedtak ->
                forslagTilVedtak(
                    BehandlingObservatør.BehandlingEvent.ForslagTilVedtak(
                        ident,
                        behandlingId,
                        søknadId,
                    ),
                )
        }
    }
}
