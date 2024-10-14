package no.nav.dagpenger.regel

import no.nav.dagpenger.opplysning.Forretningsprosess
import no.nav.dagpenger.opplysning.Opplysninger
import no.nav.dagpenger.opplysning.Opplysningstype
import no.nav.dagpenger.opplysning.Regelsett
import no.nav.dagpenger.regel.fastsetting.Dagpengegrunnlag
import no.nav.dagpenger.regel.fastsetting.DagpengenesStørrelse
import no.nav.dagpenger.regel.fastsetting.Dagpengeperiode
import no.nav.dagpenger.regel.fastsetting.Egenandel

// @todo: Snu avhengighetet  - søknadinnsendtHendelse bør leve i "dagpenger"
val støtterInnvilgelseOpplysningstype = Opplysningstype.somBoolsk("støtterInnvilgelse")

class Søknadsprosess : Forretningsprosess {
    private val regelverk = RegelverkDagpenger

    override fun regelsett(opplysninger: Opplysninger): List<Regelsett> {
        val opplysningstype = avklar(opplysninger)

        val harKravPåDagpenger =
            opplysninger.har(KravPåDagpenger.kravPåDagpenger) && opplysninger.finnOpplysning(KravPåDagpenger.kravPåDagpenger).verdi

        val regelsettFor = regelverk.regelsettFor(opplysningstype).toMutableSet()
        val støtterInnvilgelse =
            opplysninger.har(støtterInnvilgelseOpplysningstype) &&
                opplysninger.finnOpplysning(støtterInnvilgelseOpplysningstype).verdi

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

        val støtterInnvilgelse =
            opplysninger.har(støtterInnvilgelseOpplysningstype) &&
                opplysninger.finnOpplysning(støtterInnvilgelseOpplysningstype).verdi

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
