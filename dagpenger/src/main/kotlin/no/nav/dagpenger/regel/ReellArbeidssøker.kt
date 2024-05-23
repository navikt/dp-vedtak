package no.nav.dagpenger.regel

import no.nav.dagpenger.opplysning.Opplysningstype
import no.nav.dagpenger.opplysning.Regelsett
import no.nav.dagpenger.opplysning.id
import no.nav.dagpenger.opplysning.regel.alle
import no.nav.dagpenger.opplysning.regel.enAv
import no.nav.dagpenger.opplysning.regel.oppslag
import no.nav.dagpenger.regel.Behov.HelseTilAlleTyperJobb
import no.nav.dagpenger.regel.Behov.KanJobbeDeltid
import no.nav.dagpenger.regel.Behov.KanJobbeHvorSomHelst
import no.nav.dagpenger.regel.Behov.VilligTilÅBytteYrke
import no.nav.dagpenger.regel.Søknadstidspunkt.søknadstidspunkt

object ReellArbeidssøker {
    internal val kanJobbeDeltid = Opplysningstype.somBoolsk("Kan jobbe heltid og deltid".id(KanJobbeDeltid))
    internal val kanJobbeHvorSomHelst = Opplysningstype.somBoolsk("Kan jobbe i hele Norge".id(KanJobbeHvorSomHelst))
    internal val helseTilAlleTyperArbeid = Opplysningstype.somBoolsk("Kan ta alle typer arbeid".id(HelseTilAlleTyperJobb))
    internal val villigTilEthvertArbeid = Opplysningstype.somBoolsk("Villig til å bytte yrke".id(VilligTilÅBytteYrke))

    private val oppfyllerKravTilMobilitet = Opplysningstype.somBoolsk("Bruker oppfyller kravet til mobilitet")
    private val oppfyllerKravTilArbeidssøker = Opplysningstype.somBoolsk("Bruker oppfyller kravet til å være arbeidssøker")

    internal val godkjentLokalArbeidssøker = Opplysningstype.somBoolsk("Det er godkjent at bruker kun søk arbeid lokalt")
    internal val godkjentDeltidssøker = Opplysningstype.somBoolsk("Det er godkjent at bruker kun søker deltidsarbeid")

    val kravTilArbeidssøker = Opplysningstype.somBoolsk("Krav til arbeidssøker")

    val regelsett =
        Regelsett("Reell arbeidssøker") {
            regel(godkjentLokalArbeidssøker) { oppslag(søknadstidspunkt) { false } }
            regel(godkjentDeltidssøker) { oppslag(søknadstidspunkt) { false } }

            regel(oppfyllerKravTilMobilitet) { enAv(kanJobbeHvorSomHelst, godkjentLokalArbeidssøker) }
            regel(oppfyllerKravTilArbeidssøker) { enAv(kanJobbeDeltid, godkjentDeltidssøker) }
            regel(kravTilArbeidssøker) {
                alle(
                    oppfyllerKravTilArbeidssøker,
                    oppfyllerKravTilMobilitet,
                    helseTilAlleTyperArbeid,
                    villigTilEthvertArbeid,
                )
            }
        }
}
