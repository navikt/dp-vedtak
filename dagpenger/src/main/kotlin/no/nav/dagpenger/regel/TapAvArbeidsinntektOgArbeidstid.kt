package no.nav.dagpenger.regel

import no.nav.dagpenger.avklaring.Kontrollpunkt
import no.nav.dagpenger.opplysning.LesbarOpplysninger
import no.nav.dagpenger.opplysning.Opplysningsformål.Mellomsteg
import no.nav.dagpenger.opplysning.Opplysningstype
import no.nav.dagpenger.opplysning.Opplysningstype.Companion.aldriSynlig
import no.nav.dagpenger.opplysning.Regelsett
import no.nav.dagpenger.opplysning.id
import no.nav.dagpenger.opplysning.regel.alle
import no.nav.dagpenger.opplysning.regel.enAv
import no.nav.dagpenger.opplysning.regel.hvisSannMedResultat
import no.nav.dagpenger.opplysning.regel.ikke
import no.nav.dagpenger.opplysning.regel.innhentMed
import no.nav.dagpenger.opplysning.regel.minstAv
import no.nav.dagpenger.opplysning.regel.oppslag
import no.nav.dagpenger.opplysning.regel.prosentTerskel
import no.nav.dagpenger.opplysning.regel.størreEnnEllerLik
import no.nav.dagpenger.regel.Behov.HarTaptArbeid
import no.nav.dagpenger.regel.Behov.KravPåLønn
import no.nav.dagpenger.regel.Behov.ØnsketArbeidstid
import no.nav.dagpenger.regel.Samordning.samordnetBeregnetArbeidstid
import no.nav.dagpenger.regel.Samordning.uføre
import no.nav.dagpenger.regel.Søknadstidspunkt.prøvingsdato
import no.nav.dagpenger.regel.Søknadstidspunkt.søknadIdOpplysningstype
import no.nav.dagpenger.regel.fastsetting.VernepliktFastsetting.grunnlagForVernepliktErGunstigst
import no.nav.dagpenger.regel.fastsetting.VernepliktFastsetting.vernepliktFastsattVanligArbeidstid

object TapAvArbeidsinntektOgArbeidstid {
    internal val tapAvArbeid = Opplysningstype.somBoolsk("Har tapt arbeid".id(HarTaptArbeid))
    internal val kravPåLønn = Opplysningstype.somBoolsk("Krav på lønn fra tidligere arbeidsgiver".id(KravPåLønn))
    internal val ønsketArbeidstid = Opplysningstype.somDesimaltall("Ønsket arbeidstid".id(ØnsketArbeidstid))
    private val ikkeKravPåLønn = Opplysningstype.somBoolsk("Ikke krav på lønn fra tidligere arbeidsgiver", Mellomsteg, aldriSynlig)
    val kravTilTapAvArbeidsinntekt = Opplysningstype.somBoolsk("Krav til tap av arbeidsinntekt")

    private val kravTilArbeidstidsreduksjon = Opplysningstype.somDesimaltall("Krav til prosentvis tap av arbeidstid")
    private val beregningsregel = Opplysningstype.somBoolsk("Beregningsregel: Tapt arbeidstid", Mellomsteg, aldriSynlig)

    private val synlig1: (LesbarOpplysninger) -> Boolean = { it.verdiAv(beregningsregel6mnd) }
    private val synlig2: (LesbarOpplysninger) -> Boolean = { it.verdiAv(beregningsregel12mnd) }
    private val synlig3: (LesbarOpplysninger) -> Boolean = { it.verdiAv(beregningsregel36mnd) }
    internal val beregningsregel6mnd = Opplysningstype.somBoolsk("Beregningsregel: Arbeidstid siste 6 måneder", Mellomsteg, synlig1)
    private val beregningsregel12mnd = Opplysningstype.somBoolsk("Beregningsregel: Arbeidstid siste 12 måneder", Mellomsteg, synlig2)
    private val beregningsregel36mnd = Opplysningstype.somBoolsk("Beregeningsregel: Arbeidstid siste 36 måneder", Mellomsteg, synlig3)

    val beregnetArbeidstid = Opplysningstype.somDesimaltall("Beregnet vanlig arbeidstid per uke før tap")
    private val maksimalVanligArbeidstid = Opplysningstype.somDesimaltall("Maksimal vanlig arbeidstid", Mellomsteg, aldriSynlig)
    val minimumVanligArbeidstid =
        Opplysningstype.somDesimaltall("Minimum vanlig arbeidstid", Mellomsteg) { it.verdiAv(uføre) }
    val fastsattVanligArbeidstid = Opplysningstype.somDesimaltall("Fastsatt arbeidstid per uke før tap")
    val nyArbeidstid = Opplysningstype.somDesimaltall("Ny arbeidstid per uke")

    internal val ordinærEllerVernepliktArbeidstid =
        Opplysningstype.somDesimaltall("Fastsatt vanlig arbeidstid etter ordinær eller verneplikt")
    val kravTilMinstTaptArbeidstid = Opplysningstype.somBoolsk("Fastsatt vanlig arbeidstid er minst minimum arbeidstid")
    val kravTilTaptArbeidstid = Opplysningstype.somBoolsk("Tap av arbeidstid er minst terskel")
    val kravTilTapAvArbeidsinntektOgArbeidstid = Opplysningstype.somBoolsk("Krav til tap av arbeidsinntekt og arbeidstid")

    val regelsett =
        Regelsett(
            folketrygden.hjemmel(4, 3, "Krav til tap av arbeidsinntekt og arbeidstid", "4-3 Tap av arbeidsinntekt og arbeidstid"),
        ) {
            regel(ønsketArbeidstid) { innhentMed(søknadIdOpplysningstype) }

            regel(tapAvArbeid) { oppslag(prøvingsdato) { true } } // TODO: Satt til true for testing av innvilgelse
            regel(kravPåLønn) { oppslag(prøvingsdato) { false } }
            regel(ikkeKravPåLønn) { ikke(kravPåLønn) }
            regel(kravTilTapAvArbeidsinntekt) { alle(tapAvArbeid, ikkeKravPåLønn) }

            regel(kravTilArbeidstidsreduksjon) { oppslag(prøvingsdato) { 50.0 } } // Perm og sånt har andre terskelverdier

            // TODO: Kun en av disse må være sann. Enforces med Avklaring (i framtiden)
            regel(beregningsregel6mnd) { oppslag(prøvingsdato) { true } } // TODO: Satt til true for testing av innvilgelse
            regel(beregningsregel12mnd) { oppslag(prøvingsdato) { false } }
            regel(beregningsregel36mnd) { oppslag(prøvingsdato) { false } }

            // TODO: Bør hentes fra noe
            regel(beregnetArbeidstid) { oppslag(prøvingsdato) { 37.5 } } // TODO: Satt til 37.5 for testing av innvilgelse

            // FVA fra verneplikt overstyrer ordinær FVA om verneplikt er gunstigst
            regel(ordinærEllerVernepliktArbeidstid) {
                hvisSannMedResultat(grunnlagForVernepliktErGunstigst, vernepliktFastsattVanligArbeidstid, beregnetArbeidstid)
            }

            regel(nyArbeidstid) { oppslag(prøvingsdato) { 0.0 } }
            regel(maksimalVanligArbeidstid) { oppslag(prøvingsdato) { 40.0 } }
            regel(minimumVanligArbeidstid) { oppslag(prøvingsdato) { 18.75 } }

            regel(fastsattVanligArbeidstid) {
                minstAv(
                    maksimalVanligArbeidstid,
                    ordinærEllerVernepliktArbeidstid,
                    samordnetBeregnetArbeidstid,
                    ønsketArbeidstid,
                )
            }

            regel(kravTilMinstTaptArbeidstid) { størreEnnEllerLik(fastsattVanligArbeidstid, minimumVanligArbeidstid) }
            regel(kravTilTaptArbeidstid) { prosentTerskel(nyArbeidstid, fastsattVanligArbeidstid, kravTilArbeidstidsreduksjon) }

            regel(beregningsregel) { enAv(beregningsregel6mnd, beregningsregel12mnd, beregningsregel36mnd) }

            utfall(kravTilTapAvArbeidsinntektOgArbeidstid) {
                alle(kravTilTapAvArbeidsinntekt, kravTilTaptArbeidstid, beregningsregel, kravTilMinstTaptArbeidstid)
            }
        }

    val TapArbeidstidBeregningsregelKontroll =
        Kontrollpunkt(sjekker = Avklaringspunkter.TapAvArbeidstidBeregningsregel) { opplysninger ->
            if (opplysninger.mangler(beregnetArbeidstid)) {
                return@Kontrollpunkt false
            }

            listOf(
                opplysninger.har(beregningsregel6mnd) && opplysninger.finnOpplysning(beregningsregel6mnd).verdi,
                opplysninger.har(beregningsregel12mnd) && opplysninger.finnOpplysning(beregningsregel12mnd).verdi,
                opplysninger.har(beregningsregel36mnd) && opplysninger.finnOpplysning(beregningsregel36mnd).verdi,
            ).count { it } != 1
        }
}
