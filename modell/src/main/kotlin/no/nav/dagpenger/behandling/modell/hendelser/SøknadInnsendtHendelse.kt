package no.nav.dagpenger.behandling.modell.hendelser

import no.nav.dagpenger.behandling.konfigurasjon.støtterInnvilgelse
import no.nav.dagpenger.behandling.modell.Behandling
import no.nav.dagpenger.opplysning.Faktum
import no.nav.dagpenger.opplysning.Forretningsprosess
import no.nav.dagpenger.opplysning.LesbarOpplysninger
import no.nav.dagpenger.opplysning.Opplysninger
import no.nav.dagpenger.opplysning.Opplysningstype
import no.nav.dagpenger.opplysning.Regelkjøring
import no.nav.dagpenger.opplysning.Regelsett
import no.nav.dagpenger.opplysning.Systemkilde
import no.nav.dagpenger.regel.Alderskrav
import no.nav.dagpenger.regel.Alderskrav.HattLukkedeSakerSiste8UkerKontroll
import no.nav.dagpenger.regel.Alderskrav.MuligGjenopptakKontroll
import no.nav.dagpenger.regel.Alderskrav.Under18Kontroll
import no.nav.dagpenger.regel.KravPåDagpenger
import no.nav.dagpenger.regel.KravPåDagpenger.Totrinnskontroll
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
import no.nav.dagpenger.regel.fastsetting.Dagpengegrunnlag
import no.nav.dagpenger.regel.fastsetting.DagpengenesStørrelse
import no.nav.dagpenger.regel.fastsetting.Dagpengeperiode
import no.nav.dagpenger.regel.fastsetting.Egenandel
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
        return Regelkjøring(skjedde, opplysninger, Søknadsprosess())

        /*val opplysningstype = avklarer(opplysninger)
        val harKravPåDagpenger =
            opplysninger.har(KravPåDagpenger.kravPåDagpenger) && opplysninger.finnOpplysning(KravPåDagpenger.kravPåDagpenger).verdi

        val regelsettFor = regelsettFor(opplysningstype).toMutableSet()
        if (harKravPåDagpenger) {
            val fastsetting =
                RegelverkDagpenger.regelsettFor(Dagpengeperiode.antallStønadsuker) +
                    RegelverkDagpenger.regelsettFor(Dagpengegrunnlag.grunnlag) +
                    RegelverkDagpenger.regelsettFor(DagpengenesStørrelse.dagsatsMedBarn) +
                    RegelverkDagpenger.regelsettFor(Egenandel.egenandel)
            regelsettFor.addAll(fastsetting)
        }
        return Regelkjøring(skjedde, opplysninger, *regelsettFor.toTypedArray())*/
    }

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
        /*if (opplysninger.mangler(KravPåDagpenger.kravPåDagpenger)) return KravPåDagpenger.kravPåDagpenger
        val kravPåDagpenger = opplysninger.finnOpplysning(KravPåDagpenger.kravPåDagpenger).verdi
        if (!kravPåDagpenger) return KravPåDagpenger.kravPåDagpenger

        // Fastsettelse av dagpengegrunnlag, dagpengens størrelse og dagpengeperiode
        return when {
            opplysninger.mangler(Dagpengeperiode.antallStønadsuker) -> Dagpengeperiode.antallStønadsuker
            opplysninger.mangler(Dagpengegrunnlag.grunnlag) -> Dagpengegrunnlag.grunnlag
            opplysninger.mangler(DagpengenesStørrelse.dagsatsMedBarn) -> DagpengenesStørrelse.dagsatsMedBarn
            opplysninger.mangler(Egenandel.egenandel) -> Egenandel.egenandel
            else -> KravPåDagpenger.kravPåDagpenger
        }*/
    }

    private companion object {
        val fagsakIdOpplysningstype = Opplysningstype.somHeltall("fagsakId")
    }

    override fun behandling() =
        Behandling(
            this,
            listOf(
                Faktum(fagsakIdOpplysningstype, fagsakId, kilde = Systemkilde(meldingsreferanseId, opprettet)),
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
            Totrinnskontroll,
            Under18Kontroll,
        )
}

class Søknadsprosess : Forretningsprosess {
    private val regelverk = RegelverkDagpenger

    override fun regelsett(opplysninger: Opplysninger): List<Regelsett> {
        val opplysningstype = avklar(opplysninger)

        val harKravPåDagpenger =
            opplysninger.har(KravPåDagpenger.kravPåDagpenger) && opplysninger.finnOpplysning(KravPåDagpenger.kravPåDagpenger).verdi

        val regelsettFor = regelverk.regelsettFor(opplysningstype).toMutableSet()
        if (harKravPåDagpenger && støtterInnvilgelse) {
            val fastsetting =
                RegelverkDagpenger.regelsettFor(Dagpengeperiode.antallStønadsuker) +
                    RegelverkDagpenger.regelsettFor(Dagpengegrunnlag.grunnlag) +
                    RegelverkDagpenger.regelsettFor(DagpengenesStørrelse.dagsatsMedBarn) +
                    RegelverkDagpenger.regelsettFor(Egenandel.egenandel)
            regelsettFor.addAll(fastsetting)
        }

        return regelsettFor.toList()
    }

    private fun avklar(opplysninger: Opplysninger): Opplysningstype<Boolean> {
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
        } else if (!støtterInnvilgelse) {
            return Minsteinntekt.minsteinntekt
        }

        return KravPåDagpenger.kravPåDagpenger
    }
}
