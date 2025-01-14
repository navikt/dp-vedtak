package no.nav.dagpenger.regel

import no.nav.dagpenger.opplysning.Opplysningstype
import no.nav.dagpenger.opplysning.Regelsett
import no.nav.dagpenger.opplysning.id
import no.nav.dagpenger.opplysning.regel.alle
import no.nav.dagpenger.opplysning.regel.enAv
import no.nav.dagpenger.opplysning.regel.erSann
import no.nav.dagpenger.opplysning.regel.oppslag
import no.nav.dagpenger.regel.Søknadstidspunkt.prøvingsdato

object Opphold {
    var oppholdINorge = Opplysningstype.somBoolsk("Opphold i Norge".id("OppholdINorge"))
    var unntakForOpphold = Opplysningstype.somBoolsk("Oppfyller unntak for opphold i Norge")
    val oppfyllerKravetTilOpphold = Opplysningstype.somBoolsk("Oppfyller kravet til opphold i Norge eller unntak")

    val medlemFolketrygden = Opplysningstype.somBoolsk("Er personen medlem av folketrygden")
    val oppfyllerMedlemskap = Opplysningstype.somBoolsk("Oppfyller kravet til medlemskap")

    val oppfyllerKravet = Opplysningstype.somBoolsk("Oppfyller kravet til opphold i Norge")

    val regelsett =
        Regelsett(
            folketrygden.hjemmel(4, 2, "Opphold i Norge", "4-2 Opphold"),
        ) {
            regel(oppholdINorge) { oppslag(prøvingsdato) { true } }
            regel(unntakForOpphold) { oppslag(prøvingsdato) { false } }
            regel(oppfyllerKravetTilOpphold) { enAv(oppholdINorge, unntakForOpphold) }

            regel(medlemFolketrygden) { oppslag(prøvingsdato) { true } }
            regel(oppfyllerMedlemskap) { erSann(medlemFolketrygden) }

            utfall(oppfyllerKravet) { alle(oppfyllerKravetTilOpphold, oppfyllerMedlemskap) }
        }
}
