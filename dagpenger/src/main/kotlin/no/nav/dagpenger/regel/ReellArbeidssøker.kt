package no.nav.dagpenger.regel

import no.nav.dagpenger.avklaring.Kontrollpunkt
import no.nav.dagpenger.opplysning.Opplysningsformål.Bruker
import no.nav.dagpenger.opplysning.Opplysningstype
import no.nav.dagpenger.opplysning.Opplysningstype.Companion.aldriSynlig
import no.nav.dagpenger.opplysning.Regelsett
import no.nav.dagpenger.opplysning.id
import no.nav.dagpenger.opplysning.regel.alle
import no.nav.dagpenger.opplysning.regel.enAv
import no.nav.dagpenger.opplysning.regel.erSann
import no.nav.dagpenger.opplysning.regel.innhentMed
import no.nav.dagpenger.opplysning.regel.innhentes
import no.nav.dagpenger.opplysning.regel.oppslag
import no.nav.dagpenger.opplysning.regel.størreEnnEllerLik
import no.nav.dagpenger.regel.Avklaringspunkter.IkkeRegistrertSomArbeidsøker
import no.nav.dagpenger.regel.Avklaringspunkter.ReellArbeidssøkerUnntak
import no.nav.dagpenger.regel.Behov.HelseTilAlleTyperJobb
import no.nav.dagpenger.regel.Behov.KanJobbeDeltid
import no.nav.dagpenger.regel.Behov.KanJobbeHvorSomHelst
import no.nav.dagpenger.regel.Behov.RegistrertSomArbeidssøker
import no.nav.dagpenger.regel.Behov.VilligTilÅBytteYrke
import no.nav.dagpenger.regel.Behov.ØnsketArbeidstid
import no.nav.dagpenger.regel.Samordning.uføre
import no.nav.dagpenger.regel.Søknadstidspunkt.prøvingsdato
import no.nav.dagpenger.regel.Søknadstidspunkt.søknadIdOpplysningstype

object ReellArbeidssøker {
    // c.	å ta arbeid uavhengig av om det er på heltid eller deltid,
    val kanJobbeDeltid = Opplysningstype.somBoolsk("Kan jobbe heltid og deltid".id(KanJobbeDeltid), Bruker)
    val godkjentDeltidssøker =
        Opplysningstype.somBoolsk("Det er godkjent at bruker kun søker deltidsarbeid", synlig = {
            it.erSann(kanJobbeDeltid) == false
        })
    val oppfyllerKravTilArbeidssøker = Opplysningstype.somBoolsk("Oppfyller kravet til heltid- og deltidsarbeid", synlig = aldriSynlig)

    // b.	å ta arbeid hvor som helst i Norge,
    val kanJobbeHvorSomHelst = Opplysningstype.somBoolsk("Kan jobbe i hele Norge".id(KanJobbeHvorSomHelst), Bruker)
    val godkjentLokalArbeidssøker =
        Opplysningstype.somBoolsk("Det er godkjent at bruker kun søk arbeid lokalt", synlig = {
            it.erSann(kanJobbeHvorSomHelst) == false
        })
    val oppfyllerKravTilMobilitet = Opplysningstype.somBoolsk("Oppfyller kravet til mobilitet", synlig = aldriSynlig)

    //  Som reell arbeidssøker regnes den som er arbeidsfør,
    val erArbeidsfør = Opplysningstype.somBoolsk("Kan ta alle typer arbeid".id(HelseTilAlleTyperJobb), Bruker)
    val oppfyllerKravTilArbeidsfør = Opplysningstype.somBoolsk("Oppfyller kravet til å være arbeidsfør", synlig = aldriSynlig)

    // a.	å ta ethvert arbeid som er lønnet etter tariff eller sedvane,
    val villigTilEthvertArbeid = Opplysningstype.somBoolsk("Villig til å bytte yrke".id(VilligTilÅBytteYrke), Bruker)
    val oppfyllerKravetTilEthvertArbeid = Opplysningstype.somBoolsk("Oppfyller kravet til å ta ethvert arbeid", synlig = aldriSynlig)

    // Registrert som arbeidssøker
    internal val registrertArbeidssøker = Opplysningstype.somBoolsk("Registrert som arbeidssøker".id(RegistrertSomArbeidssøker))
    val oppyllerKravTilRegistrertArbeidssøker =
        Opplysningstype.somBoolsk("Registrert som arbeidssøker på søknadstidspunktet", synlig = aldriSynlig)

    val kravTilArbeidssøker = Opplysningstype.somBoolsk("Krav til arbeidssøker")

    val ønsketArbeidstid =
        Opplysningstype.somDesimaltall("Ønsket arbeidstid".id(ØnsketArbeidstid), Bruker) {
            it.erSann(kanJobbeDeltid) == false
        }
    val minimumVanligArbeidstid = Opplysningstype.somDesimaltall("Minimum vanlig arbeidstid") { it.erSann(uføre) }
    val villigTilMinimumArbeidstid = Opplysningstype.somBoolsk("Villig til å jobbe minimum arbeidstid")

    val regelsett =
        Regelsett(folketrygden.hjemmel(4, 5, "Reelle arbeidssøkere", "4-5 Reell arbeidssøker")) {
            regel(ønsketArbeidstid) { innhentMed(søknadIdOpplysningstype) }
            regel(minimumVanligArbeidstid) { oppslag(prøvingsdato) { 18.75 } }
            regel(villigTilMinimumArbeidstid) { størreEnnEllerLik(ønsketArbeidstid, minimumVanligArbeidstid) }

            regel(kanJobbeDeltid) { innhentes }
            regel(godkjentDeltidssøker) { oppslag(prøvingsdato) { false } }

            regel(kanJobbeHvorSomHelst) { innhentes }
            regel(godkjentLokalArbeidssøker) { oppslag(prøvingsdato) { false } }

            regel(erArbeidsfør) { innhentes }
            regel(villigTilEthvertArbeid) { innhentes }

            regel(oppfyllerKravTilArbeidssøker) { enAv(kanJobbeDeltid, godkjentDeltidssøker) }
            regel(oppfyllerKravTilMobilitet) { enAv(kanJobbeHvorSomHelst, godkjentLokalArbeidssøker) }
            regel(oppfyllerKravTilArbeidsfør) { enAv(erArbeidsfør) }
            regel(oppfyllerKravetTilEthvertArbeid) { enAv(villigTilEthvertArbeid) }

            regel(registrertArbeidssøker) { innhentMed(prøvingsdato) }
            regel(oppyllerKravTilRegistrertArbeidssøker) { erSann(registrertArbeidssøker) }

            utfall(kravTilArbeidssøker) {
                alle(
                    villigTilMinimumArbeidstid,
                    oppfyllerKravTilArbeidssøker,
                    oppfyllerKravTilMobilitet,
                    oppfyllerKravTilArbeidsfør,
                    oppfyllerKravetTilEthvertArbeid,
                    oppyllerKravTilRegistrertArbeidssøker,
                )
            }

            avklaring(ReellArbeidssøkerUnntak)
            avklaring(IkkeRegistrertSomArbeidsøker)
        }

    val ReellArbeidssøkerKontroll =
        Kontrollpunkt(ReellArbeidssøkerUnntak) {
            it.har(kravTilArbeidssøker) && !it.finnOpplysning(kravTilArbeidssøker).verdi
        }

    val IkkeRegistrertSomArbeidsøkerKontroll =
        Kontrollpunkt(IkkeRegistrertSomArbeidsøker) {
            it.har(oppyllerKravTilRegistrertArbeidssøker) && !it.finnOpplysning(oppyllerKravTilRegistrertArbeidssøker).verdi
        }
}
