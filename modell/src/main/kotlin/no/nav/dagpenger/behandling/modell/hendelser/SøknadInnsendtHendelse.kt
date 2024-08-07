package no.nav.dagpenger.behandling.modell.hendelser

import no.nav.dagpenger.behandling.konklusjon.Konklusjon
import no.nav.dagpenger.behandling.modell.Behandling
import no.nav.dagpenger.opplysning.Faktum
import no.nav.dagpenger.opplysning.LesbarOpplysninger
import no.nav.dagpenger.opplysning.Opplysningstype
import no.nav.dagpenger.regel.Alderskrav
import no.nav.dagpenger.regel.KravPåDagpenger
import no.nav.dagpenger.regel.Minsteinntekt
import no.nav.dagpenger.regel.Minsteinntekt.EØSArbeidKontroll
import no.nav.dagpenger.regel.Minsteinntekt.InntektNesteKalendermånedKontroll
import no.nav.dagpenger.regel.Minsteinntekt.JobbetUtenforNorgeKontroll
import no.nav.dagpenger.regel.Minsteinntekt.SvangerskapsrelaterteSykepengerKontroll
import no.nav.dagpenger.regel.Minsteinntekt.ØnskerEtterRapporteringsfristKontroll
import no.nav.dagpenger.regel.RegelverkDagpenger
import no.nav.dagpenger.regel.Søknadstidspunkt.HattLukkedeSakerSiste8UkerKontroll
import no.nav.dagpenger.regel.Søknadstidspunkt.MuligGjenopptakKontroll
import no.nav.dagpenger.regel.Verneplikt.VernepliktKontroll
import no.nav.dagpenger.regel.fastsetting.Dagpengeperiode
import no.nav.dagpenger.regel.konklusjon.knockoutAvslag
import no.nav.dagpenger.regel.konklusjon.regelsettKnockout
import java.time.LocalDate
import java.util.UUID

class SøknadInnsendtHendelse(
    meldingsreferanseId: UUID,
    ident: String,
    søknadId: UUID,
    gjelderDato: LocalDate,
    fagsakId: Int,
) : StartHendelse(meldingsreferanseId, ident, SøknadId(søknadId), gjelderDato, fagsakId) {
    override fun regelsett() = RegelverkDagpenger.regelsett

    override fun avklarer(opplysninger: LesbarOpplysninger): Opplysningstype<*> {
        // Sjekk krav til alder
        if (!opplysninger.har(Alderskrav.kravTilAlder)) return Alderskrav.kravTilAlder
        // Sjekk krav til minste arbeidsinntekt
        if (!opplysninger.har(Minsteinntekt.minsteinntekt)) return Minsteinntekt.minsteinntekt

        // Om krav til alder eller arbeidsinntekt ikke er oppfylt er det ingen grunn til å fortsette
        if (!opplysninger.finnOpplysning(Minsteinntekt.minsteinntekt).verdi ||
            !opplysninger.finnOpplysning(Alderskrav.kravTilAlder).verdi
        ) {
            // TODO: return Minsteinntekt.minsteinntekt
            return KravPåDagpenger.kravPåDagpenger
        }

        if (!opplysninger.har(KravPåDagpenger.kravPåDagpenger)) return KravPåDagpenger.kravPåDagpenger
        if (!opplysninger.finnOpplysning(KravPåDagpenger.kravPåDagpenger).verdi) return KravPåDagpenger.kravPåDagpenger

        return Dagpengeperiode.antallStønadsuker
    }

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

    override fun kanKonkludere(opplysninger: LesbarOpplysninger): Boolean {
        val konklusjon = Konklusjon(opplysninger, regelsettKnockout)
        return konklusjon.kanKonkludere(knockoutAvslag)
    }

    override fun kontrollpunkter() =
        listOf(
            EØSArbeidKontroll,
            HattLukkedeSakerSiste8UkerKontroll,
            InntektNesteKalendermånedKontroll,
            JobbetUtenforNorgeKontroll,
            MuligGjenopptakKontroll,
            SvangerskapsrelaterteSykepengerKontroll,
            VernepliktKontroll,
            ØnskerEtterRapporteringsfristKontroll,
        )
}
