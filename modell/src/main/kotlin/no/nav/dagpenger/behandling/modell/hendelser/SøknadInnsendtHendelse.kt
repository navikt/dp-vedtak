package no.nav.dagpenger.behandling.modell.hendelser

import AvslagAlder
import AvslagInntekt
import Innvilgelse
import no.nav.dagpenger.avklaring.Kontrollpunkt
import no.nav.dagpenger.behandling.modell.Behandling
import no.nav.dagpenger.opplysning.Faktum
import no.nav.dagpenger.opplysning.Opplysningstype
import no.nav.dagpenger.regel.KravPåDagpenger
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

    override fun avklarer(): Opplysningstype<Boolean> = KravPåDagpenger.kravPåDagpenger

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

    override fun konklusjonStrategier() = listOf(AvslagAlder, AvslagInntekt, Innvilgelse)

    override fun kontrollpunkter(): List<Kontrollpunkt> {
        return emptyList()
    }
}
