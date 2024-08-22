package no.nav.dagpenger.behandling.modell.hendelser

import no.nav.dagpenger.behandling.modell.Behandling
import no.nav.dagpenger.opplysning.Faktum
import no.nav.dagpenger.opplysning.LesbarOpplysninger
import no.nav.dagpenger.opplysning.Opplysninger
import no.nav.dagpenger.opplysning.Opplysningstype
import no.nav.dagpenger.opplysning.Regelkjøring
import no.nav.dagpenger.regel.Alderskrav
import no.nav.dagpenger.regel.Alderskrav.HattLukkedeSakerSiste8UkerKontroll
import no.nav.dagpenger.regel.Alderskrav.MuligGjenopptakKontroll
import no.nav.dagpenger.regel.KravPåDagpenger
import no.nav.dagpenger.regel.Meldeplikt
import no.nav.dagpenger.regel.Minsteinntekt
import no.nav.dagpenger.regel.Minsteinntekt.EØSArbeidKontroll
import no.nav.dagpenger.regel.Minsteinntekt.InntektNesteKalendermånedKontroll
import no.nav.dagpenger.regel.Minsteinntekt.JobbetUtenforNorgeKontroll
import no.nav.dagpenger.regel.Minsteinntekt.SvangerskapsrelaterteSykepengerKontroll
import no.nav.dagpenger.regel.Minsteinntekt.ØnskerEtterRapporteringsfristKontroll
import no.nav.dagpenger.regel.ReellArbeidssøker
import no.nav.dagpenger.regel.RegelverkDagpenger
import no.nav.dagpenger.regel.Rettighetstype
import no.nav.dagpenger.regel.Søknadstidspunkt.SøknadstidspunktForLangtFramITid
import no.nav.dagpenger.regel.TapAvArbeidsinntektOgArbeidstid.TapArbeidstidBeregningsregelKontroll
import no.nav.dagpenger.regel.Verneplikt.VernepliktKontroll
import no.nav.dagpenger.regel.fastsetting.Dagpengeperiode
import java.time.LocalDate
import java.time.LocalDateTime
import java.util.UUID

class SøknadInnsendtHendelse(
    meldingsreferanseId: UUID,
    ident: String,
    søknadId: UUID,
    gjelderDato: LocalDate,
    fagsakId: Int,
    opprettet: LocalDateTime,
) : StartHendelse(meldingsreferanseId, ident, SøknadId(søknadId), gjelderDato, fagsakId, opprettet) {
    private fun regelsettFor(opplysning: Opplysningstype<*>) = RegelverkDagpenger.regelsettFor(opplysning)

    override fun regelkjøring(opplysninger: Opplysninger): Regelkjøring {
        val opplysningstype = avklarer(opplysninger)
        return Regelkjøring(skjedde, opplysninger, *regelsettFor(opplysningstype).toTypedArray())
    }

    override fun avklarer(opplysninger: LesbarOpplysninger): Opplysningstype<*> {
        // Sjekk krav til alder
        if (!opplysninger.har(Alderskrav.kravTilAlder)) return Alderskrav.kravTilAlder
        // Sjekk krav til minste arbeidsinntekt
        if (!opplysninger.har(Minsteinntekt.minsteinntekt)) return Minsteinntekt.minsteinntekt

        // Om krav til alder eller arbeidsinntekt ikke er oppfylt er det ingen grunn til å fortsette
        if (!opplysninger.finnOpplysning(Minsteinntekt.minsteinntekt).verdi ||
            !opplysninger.finnOpplysning(Alderskrav.kravTilAlder).verdi
        ) {
            if (opplysninger.mangler(ReellArbeidssøker.kravTilArbeidssøker)) {
                return ReellArbeidssøker.kravTilArbeidssøker
            }

            if (opplysninger.mangler(Meldeplikt.registrertPåSøknadstidspunktet)) {
                return Meldeplikt.registrertPåSøknadstidspunktet
            }
            if (opplysninger.mangler(Rettighetstype.rettighetstype)) {
                return Rettighetstype.rettighetstype
            }

            return Minsteinntekt.minsteinntekt
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

    override fun kontrollpunkter() =
        listOf(
            EØSArbeidKontroll,
            HattLukkedeSakerSiste8UkerKontroll,
            InntektNesteKalendermånedKontroll,
            JobbetUtenforNorgeKontroll,
            MuligGjenopptakKontroll,
            SvangerskapsrelaterteSykepengerKontroll,
            SøknadstidspunktForLangtFramITid,
            VernepliktKontroll,
            ØnskerEtterRapporteringsfristKontroll,
            TapArbeidstidBeregningsregelKontroll,
        )
}
