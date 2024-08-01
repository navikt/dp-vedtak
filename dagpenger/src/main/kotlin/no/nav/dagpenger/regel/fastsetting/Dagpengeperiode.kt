package no.nav.dagpenger.regel.fastsetting

import no.nav.dagpenger.opplysning.Opplysningstype
import no.nav.dagpenger.opplysning.Regelsett
import no.nav.dagpenger.opplysning.regel.divisjon
import no.nav.dagpenger.opplysning.regel.erSann
import no.nav.dagpenger.opplysning.regel.multiplikasjon
import no.nav.dagpenger.opplysning.regel.oppslag
import no.nav.dagpenger.regel.Minsteinntekt
import no.nav.dagpenger.regel.Søknadstidspunkt
import no.nav.dagpenger.regel.Verneplikt

object Dagpengeperiode {
    private val antallStønadsuker = Opplysningstype.somHeltall("Antall stønadsuker")
    private val terskelFaktor12 = Opplysningstype.somDesimaltall("Terskelfaktor for 12 måneder")
    private val terskelFaktor36 = Opplysningstype.somDesimaltall("Terskelfaktor for 36 måneder")
    private val grunnbeløp = Minsteinntekt.grunnbeløp
    private val terskel12 = Opplysningstype.somDesimaltall("Terskel for 12 måneder")
    private val terskel36 = Opplysningstype.somDesimaltall("Terskel for 36 måneder")
    private val inntektSiste12 = Minsteinntekt.inntekt12
    private val inntektSiste36 = Minsteinntekt.inntekt36

    private val inntektSnittSiste36 = Opplysningstype.somDesimaltall("Snittinntekt siste 36 måneder")
    private val divisior = Opplysningstype.somDesimaltall("Divisior")

    private val vernepliktStønadsuker = Opplysningstype.somHeltall("Stønadsuker ved verneplikt")
    private val erVernepliktig = Opplysningstype.somBoolsk("Er vernepliktig")
    private val stønadsuker12 = Opplysningstype.somHeltall("Stønadsuker ved siste 12 måneder")
    private val stønadsuker36 = Opplysningstype.somHeltall("Stønadsuker ved siste 36 måneder")
    val regelsett =
        Regelsett("Dagpengeperiode") {
            regel(terskelFaktor12) { oppslag(Søknadstidspunkt.søknadstidspunkt) { 2.0 } }
            regel(terskelFaktor36) { oppslag(Søknadstidspunkt.søknadstidspunkt) { 2.0 } }
            regel(divisior) { oppslag(Søknadstidspunkt.søknadstidspunkt) { 3.0 } }
            regel(terskel12) { multiplikasjon(terskelFaktor12, grunnbeløp) }
            regel(terskel36) { multiplikasjon(terskelFaktor36, grunnbeløp) }
            regel(inntektSnittSiste36) { divisjon(inntektSiste36, divisior) }

            regel(erVernepliktig) { erSann(Verneplikt.vurderingAvVerneplikt) }

//            regel(antallStønadsuker) {
//                //  maksAv(vernepliktStønadsuker, stønadsuker12, stønadsuker36)
//            }
        }
}
