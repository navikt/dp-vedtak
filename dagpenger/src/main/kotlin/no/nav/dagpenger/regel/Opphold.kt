package no.nav.dagpenger.regel

import no.nav.dagpenger.opplysning.Opplysningstype
import no.nav.dagpenger.opplysning.Regelsett
import no.nav.dagpenger.opplysning.id
import no.nav.dagpenger.opplysning.regel.enAv
import no.nav.dagpenger.opplysning.regel.oppslag
import no.nav.dagpenger.regel.Søknadstidspunkt.søknadstidspunkt

object Opphold {
    var oppholdINorge = Opplysningstype.somBoolsk("Opphold i Norge".id("OppholdINorge"))
    var unntakForOpphold = Opplysningstype.somBoolsk("Oppfyller unntak for opphold i Norge")
    val oppfyllerKravet = Opplysningstype.somBoolsk("Oppfyller kravet til Opphold i Norge")

    val regelsett =
        Regelsett("Opphold i Norge") {
            regel(oppholdINorge) { oppslag(søknadstidspunkt) { true } }
            regel(unntakForOpphold) { oppslag(søknadstidspunkt) { false } }

            regel(oppfyllerKravet) { enAv(oppholdINorge, unntakForOpphold) }
        }
}
