package no.nav.dagpenger.behandling.modell.hendelser

import no.nav.dagpenger.behandling.modell.Behandling
import no.nav.dagpenger.opplysning.Faktum
import no.nav.dagpenger.opplysning.Opplysningstype
import no.nav.dagpenger.regel.RettTilDagpenger
import no.nav.dagpenger.regel.SøknadInnsendtRegelsett
import java.time.LocalDate
import java.util.UUID

class SøknadInnsendtHendelse(
    meldingsreferanseId: UUID,
    ident: String,
    søknadId: UUID,
    gjelderDato: LocalDate,
    fagsakId: Int,
) : StartHendelse(meldingsreferanseId, ident, SøknadId(søknadId), gjelderDato, fagsakId) {
    override fun regelsett() = SøknadInnsendtRegelsett.regelsett

    override fun avklarer(): Opplysningstype<Boolean> = RettTilDagpenger.kravPåDagpenger

    private companion object {
        val fagsakIdOpplysningstype = Opplysningstype.somHeltall("fagsakId")
    }

    override fun behandling() =
        Behandling(
            this,
            listOf(
                Faktum(fagsakIdOpplysningstype, fagsakId),
            ),
        )
}
