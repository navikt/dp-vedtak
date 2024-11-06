package no.nav.dagpenger.regel

import no.nav.dagpenger.avklaring.Avklaring
import no.nav.dagpenger.behandling.modell.Behandling
import no.nav.dagpenger.behandling.modell.hendelser.StartHendelse
import no.nav.dagpenger.behandling.modell.hendelser.SøknadId
import no.nav.dagpenger.opplysning.Faktum
import no.nav.dagpenger.opplysning.Gyldighetsperiode
import no.nav.dagpenger.opplysning.LesbarOpplysninger
import no.nav.dagpenger.opplysning.Opplysninger
import no.nav.dagpenger.opplysning.Opplysningstype
import no.nav.dagpenger.opplysning.Regelkjøring
import no.nav.dagpenger.opplysning.Systemkilde
import no.nav.dagpenger.regel.Alderskrav.HattLukkedeSakerSiste8UkerKontroll
import no.nav.dagpenger.regel.Alderskrav.MuligGjenopptakKontroll
import no.nav.dagpenger.regel.Alderskrav.Under18Kontroll
import no.nav.dagpenger.regel.KravPåDagpenger.Totrinnskontroll
import no.nav.dagpenger.regel.Minsteinntekt.EØSArbeidKontroll
import no.nav.dagpenger.regel.Minsteinntekt.InntektNesteKalendermånedKontroll
import no.nav.dagpenger.regel.Minsteinntekt.JobbetUtenforNorgeKontroll
import no.nav.dagpenger.regel.Minsteinntekt.SvangerskapsrelaterteSykepengerKontroll
import no.nav.dagpenger.regel.Minsteinntekt.ØnskerEtterRapporteringsfristKontroll
import no.nav.dagpenger.regel.Søknadstidspunkt.SøknadstidspunktForLangtFramITid
import no.nav.dagpenger.regel.Søknadstidspunkt.søknadIdOpplysningstype
import no.nav.dagpenger.regel.TapAvArbeidsinntektOgArbeidstid.TapArbeidstidBeregningsregelKontroll
import no.nav.dagpenger.regel.Verneplikt.VernepliktKontroll
import no.nav.dagpenger.regel.Virkningstidspunkt.VirkningstidspunktForLangtFramITid
import no.nav.dagpenger.regel.fastsetting.DagpengenesStørrelse.BarnetilleggKontroll
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
    private val støtterInnvilgelse: Boolean = false,
) : StartHendelse(meldingsreferanseId, ident, SøknadId(søknadId), gjelderDato, fagsakId, opprettet) {
    override fun regelkjøring(opplysninger: Opplysninger): Regelkjøring = Regelkjøring(skjedde, opplysninger, Søknadsprosess())

    // todo: Behovet forsvinner når vi forslag til vedtak og vedtak_fattet. De forventer at det vi avklarer er en boolean.
    override fun avklarer(opplysninger: LesbarOpplysninger): Opplysningstype<Boolean> {
        // Sjekk krav til alder
        if (!opplysninger.har(Alderskrav.kravTilAlder)) return Alderskrav.kravTilAlder
        // Sjekk krav til minste arbeidsinntekt
        if (!opplysninger.har(Minsteinntekt.minsteinntekt)) return Minsteinntekt.minsteinntekt

        val alderskravOppfylt = opplysninger.finnOpplysning(Alderskrav.kravTilAlder).verdi
        val minsteinntektOppfylt = opplysninger.finnOpplysning(Minsteinntekt.minsteinntekt).verdi

        // Om krav til alder eller arbeidsinntekt ikke er oppfylt er det ingen grunn til å fortsette, men vi må fastsette hvilke tillegskrav Arena trenger.
        if (!alderskravOppfylt || !minsteinntektOppfylt) {
            return when {
                opplysninger.mangler(ReellArbeidssøker.kravTilArbeidssøker) -> ReellArbeidssøker.kravTilArbeidssøker
                opplysninger.mangler(Meldeplikt.registrertPåSøknadstidspunktet) -> Meldeplikt.registrertPåSøknadstidspunktet
                opplysninger.mangler(Rettighetstype.rettighetstype) -> Rettighetstype.rettighetstype
                else -> Minsteinntekt.minsteinntekt
            }
        }

        return KravPåDagpenger.kravPåDagpenger
    }

    override fun prøvingsdato(opplysninger: LesbarOpplysninger): LocalDate =
        if (opplysninger.har(Søknadstidspunkt.prøvingsdato)) opplysninger.finnOpplysning(Søknadstidspunkt.prøvingsdato).verdi else skjedde

    override fun støtterInnvilgelse(opplysninger: LesbarOpplysninger): Boolean =
        opplysninger.har(støtterInnvilgelseOpplysningstype) &&
            opplysninger.finnOpplysning(støtterInnvilgelseOpplysningstype).verdi

    override fun kravPåDagpenger(opplysninger: LesbarOpplysninger): Boolean =
        opplysninger.har(KravPåDagpenger.kravPåDagpenger) &&
            opplysninger.finnOpplysning(KravPåDagpenger.kravPåDagpenger).verdi

    override fun minsteinntekt(opplysninger: LesbarOpplysninger): Boolean =
        opplysninger.har(Minsteinntekt.minsteinntekt) &&
            opplysninger.finnOpplysning(Minsteinntekt.minsteinntekt).verdi

    override fun kreverTotrinnskontroll(aktiveAvklaringer: List<Avklaring>): Boolean =
        aktiveAvklaringer.any {
            it.kode == Avklaringspunkter.Totrinnskontroll
        }

    override fun behandling() =
        Behandling(
            behandler = this,
            opplysninger =
                listOf(
                    // Faktum(prøvingsdato, skjedde, kilde = Systemkilde(meldingsreferanseId, opprettet)),
                    Faktum(fagsakIdOpplysningstype, fagsakId, kilde = Systemkilde(meldingsreferanseId, opprettet)),
                    Faktum(
                        søknadIdOpplysningstype,
                        this.eksternId.id.toString(),
                        Gyldighetsperiode(skjedde, skjedde),
                        kilde = Systemkilde(meldingsreferanseId, opprettet),
                    ),
                    Faktum(
                        støtterInnvilgelseOpplysningstype,
                        støtterInnvilgelse,
                        kilde = Systemkilde(meldingsreferanseId, opprettet),
                    ),
                ),
        )

    override fun kontrollpunkter() =
        listOf(
            BarnetilleggKontroll,
            EØSArbeidKontroll,
            HattLukkedeSakerSiste8UkerKontroll,
            InntektNesteKalendermånedKontroll,
            JobbetUtenforNorgeKontroll,
            MuligGjenopptakKontroll,
            SvangerskapsrelaterteSykepengerKontroll,
            SøknadstidspunktForLangtFramITid,
            TapArbeidstidBeregningsregelKontroll,
            Totrinnskontroll,
            Under18Kontroll,
            VernepliktKontroll,
            VirkningstidspunktForLangtFramITid,
            ØnskerEtterRapporteringsfristKontroll,
        )

    companion object {
        val fagsakIdOpplysningstype = Opplysningstype.somHeltall("fagsakId")
    }
}
