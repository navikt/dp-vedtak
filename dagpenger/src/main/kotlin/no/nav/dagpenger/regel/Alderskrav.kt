package no.nav.dagpenger.regel

import no.nav.dagpenger.avklaring.Kontrollpunkt
import no.nav.dagpenger.opplysning.Opplysningstype
import no.nav.dagpenger.opplysning.Opplysningstype.Companion.aldriSynlig
import no.nav.dagpenger.opplysning.Regelsett
import no.nav.dagpenger.opplysning.id
import no.nav.dagpenger.opplysning.regel.dato.førEllerLik
import no.nav.dagpenger.opplysning.regel.dato.leggTilÅr
import no.nav.dagpenger.opplysning.regel.dato.sisteDagIMåned
import no.nav.dagpenger.opplysning.regel.innhentes
import no.nav.dagpenger.opplysning.regel.oppslag
import no.nav.dagpenger.regel.Søknadstidspunkt.søknadsdato

object Alderskrav {
    val fødselsdato = Opplysningstype.somDato("Fødselsdato".id("Fødselsdato", "opplysning.fodselsdato"))

    private val prøvingsdato = Søknadstidspunkt.prøvingsdato

    private val aldersgrense = Opplysningstype.somHeltall("Aldersgrense", synlig = aldriSynlig)
    private val sisteMåned = Opplysningstype.somDato("Dato søker når maks alder", synlig = aldriSynlig)
    private val sisteDagIMåned = Opplysningstype.somDato("Siste mulige dag bruker kan oppfylle alderskrav")

    val kravTilAlder = Opplysningstype.somBoolsk("Oppfyller kravet til alder")

    val regelsett =
        Regelsett(
            folketrygden.hjemmel(4, 23, "Bortfall på grunn av alder", "4-23 Alder"),
        ) {
            regel(fødselsdato) { innhentes }
            regel(aldersgrense) { oppslag(prøvingsdato) { 67 } }
            regel(sisteMåned) { leggTilÅr(fødselsdato, aldersgrense) }
            regel(sisteDagIMåned) { sisteDagIMåned(sisteMåned) }

            utfall(kravTilAlder) { førEllerLik(prøvingsdato, sisteDagIMåned) }
        }

    val MuligGjenopptakKontroll =
        Kontrollpunkt(Avklaringspunkter.MuligGjenopptak) { it.har(kravTilAlder) && it.finnOpplysning(kravTilAlder).verdi }

    val HattLukkedeSakerSiste8UkerKontroll =
        Kontrollpunkt(Avklaringspunkter.HattLukkedeSakerSiste8Uker) { it.har(kravTilAlder) && it.finnOpplysning(kravTilAlder).verdi }

    val Under18Kontroll =
        Kontrollpunkt(Avklaringspunkter.BrukerUnder18) {
            if (it.mangler(fødselsdato) || it.mangler(søknadsdato)) {
                return@Kontrollpunkt false
            }
            val søknadsdato = it.finnOpplysning(søknadsdato).verdi
            val fødselsdato = it.finnOpplysning(fødselsdato).verdi

            søknadsdato.minusYears(18).isBefore(fødselsdato)
        }
}
