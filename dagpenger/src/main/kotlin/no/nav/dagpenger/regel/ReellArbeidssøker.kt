package no.nav.dagpenger.regel

import no.nav.dagpenger.avklaring.Avklaringkode
import no.nav.dagpenger.avklaring.Kontrollpunkt
import no.nav.dagpenger.opplysning.Opplysningstype
import no.nav.dagpenger.opplysning.Regelsett
import no.nav.dagpenger.opplysning.id
import no.nav.dagpenger.opplysning.regel.alle
import no.nav.dagpenger.opplysning.regel.enAv
import no.nav.dagpenger.opplysning.regel.innhentes
import no.nav.dagpenger.opplysning.regel.oppslag
import no.nav.dagpenger.opplysning.regel.størreEnnEllerLik
import no.nav.dagpenger.regel.Avklaringspunkter.ReellArbeidssøkerUnntak
import no.nav.dagpenger.regel.Behov.HelseTilAlleTyperJobb
import no.nav.dagpenger.regel.Behov.KanJobbeDeltid
import no.nav.dagpenger.regel.Behov.KanJobbeHvorSomHelst
import no.nav.dagpenger.regel.Behov.VilligTilÅBytteYrke
import no.nav.dagpenger.regel.Behov.ØnsketArbeidstid
import no.nav.dagpenger.regel.Søknadstidspunkt.prøvingsdato

object ReellArbeidssøker {
    internal val kanJobbeDeltid = Opplysningstype.somBoolsk("Kan jobbe heltid og deltid".id(KanJobbeDeltid))
    internal val kanJobbeHvorSomHelst = Opplysningstype.somBoolsk("Kan jobbe i hele Norge".id(KanJobbeHvorSomHelst))
    internal val helseTilAlleTyperArbeid = Opplysningstype.somBoolsk("Kan ta alle typer arbeid".id(HelseTilAlleTyperJobb))
    internal val villigTilEthvertArbeid = Opplysningstype.somBoolsk("Villig til å bytte yrke".id(VilligTilÅBytteYrke))

    private val oppfyllerKravTilMobilitet = Opplysningstype.somBoolsk("Bruker oppfyller kravet til mobilitet")
    private val oppfyllerKravTilArbeidssøker = Opplysningstype.somBoolsk("Bruker oppfyller kravet til å være arbeidssøker")

    internal val ønsketArbeidstid = Opplysningstype.somDesimaltall("Brukers ønskede arbeidstid".id(ØnsketArbeidstid))
    private val minsteMuligeArbeidstid = Opplysningstype.somDesimaltall("Nedre grense for ønsket arbeidstid")
    private val ønsketArbeidstidErOverTerskel = Opplysningstype.somBoolsk("Ønsket arbeidstid er under terskel")

    val godkjentLokalArbeidssøker = Opplysningstype.somBoolsk("Det er godkjent at bruker kun søk arbeid lokalt")
    val godkjentDeltidssøker = Opplysningstype.somBoolsk("Det er godkjent at bruker kun søker deltidsarbeid")

    val kravTilArbeidssøker = Opplysningstype.somBoolsk("Krav til arbeidssøker")

    val regelsett =
        Regelsett("Reell arbeidssøker") {
            regel(kanJobbeDeltid) { innhentes }
            regel(kanJobbeHvorSomHelst) { innhentes }
            regel(helseTilAlleTyperArbeid) { innhentes }
            regel(villigTilEthvertArbeid) { innhentes }

            regel(godkjentLokalArbeidssøker) { oppslag(prøvingsdato) { false } }
            regel(godkjentDeltidssøker) { oppslag(prøvingsdato) { false } }

            regel(ønsketArbeidstid) { innhentes } // Settes til 40 om den ikke finnes
            regel(minsteMuligeArbeidstid) { oppslag(prøvingsdato) { 18.75 } } // Bør kunne være 11.25 om ufør
            regel(ønsketArbeidstidErOverTerskel) { størreEnnEllerLik(ønsketArbeidstid, minsteMuligeArbeidstid) }

            regel(oppfyllerKravTilArbeidssøker) { enAv(kanJobbeDeltid, godkjentDeltidssøker) }
            regel(oppfyllerKravTilMobilitet) { enAv(kanJobbeHvorSomHelst, godkjentLokalArbeidssøker) }

            regel(kravTilArbeidssøker) {
                alle(
                    oppfyllerKravTilArbeidssøker,
                    oppfyllerKravTilMobilitet,
                    helseTilAlleTyperArbeid,
                    villigTilEthvertArbeid,
                    ønsketArbeidstidErOverTerskel,
                )
            }
        }

    val ReellArbeidssøkerKontroll =
        Kontrollpunkt(ReellArbeidssøkerUnntak) {
            it.har(kravTilArbeidssøker) && !it.finnOpplysning(kravTilArbeidssøker).verdi
        }

    val ØnsketArbeidstidKontroll =
        Kontrollpunkt(
            Avklaringkode(
                kode = "ØnsketArbeidstid",
                tittel = "Ønsket arbeidstid er under terskel",
                beskrivelse = "Bruker har oppgitt en ønsket arbeidstid som er under terskel",
            ),
        ) {
            it.har(ønsketArbeidstidErOverTerskel) && !it.finnOpplysning(ønsketArbeidstidErOverTerskel).verdi
        }
}
