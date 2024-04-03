package no.nav.dagpenger.behandling.modell.hendelser

import no.nav.dagpenger.regel.Alderskrav
import no.nav.dagpenger.regel.Meldeplikt
import no.nav.dagpenger.regel.Minsteinntekt
import no.nav.dagpenger.regel.Opptjeningstid
import no.nav.dagpenger.regel.ReellArbeidssøker
import no.nav.dagpenger.regel.RettTilDagpenger
import no.nav.dagpenger.regel.Rettighetstype
import no.nav.dagpenger.regel.Søknadstidspunkt
import java.time.LocalDate
import java.util.UUID

class SøknadInnsendtHendelse(
    meldingsreferanseId: UUID,
    ident: String,
    søknadId: UUID,
    gjelderDato: LocalDate,
    fagsakId: Int,
) : StartHendelse(meldingsreferanseId, ident, SøknadId(søknadId), gjelderDato, fagsakId) {
    override fun regelsett() =
        listOf(
            Alderskrav.regelsett,
            Meldeplikt.regelsett,
            Minsteinntekt.regelsett,
            Opptjeningstid.regelsett,
            ReellArbeidssøker.regelsett,
            RettTilDagpenger.regelsett,
            Rettighetstype.regelsett,
            Søknadstidspunkt.regelsett,
        )

    override fun avklarer() = RettTilDagpenger.kravPåDagpenger
}
