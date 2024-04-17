package no.nav.dagpenger.regel

import no.nav.dagpenger.opplysning.Opplysningstype
import no.nav.dagpenger.opplysning.Regelsett
import no.nav.dagpenger.opplysning.id
import no.nav.dagpenger.opplysning.regel.dato.førEllerLik
import no.nav.dagpenger.opplysning.regel.dato.leggTilDager
import no.nav.dagpenger.opplysning.regel.dato.sisteAv
import no.nav.dagpenger.opplysning.regel.oppslag

object Søknadstidspunkt {
    val søknadsdato = Opplysningstype.somDato("Søknadsdato".id("Søknadstidspunkt"))
    val ønsketdato = Opplysningstype.somDato("Ønsker dagpenger fra dato".id("ØnskerDagpengerFraDato"))

    // TODO: vi må rydde i begrepsbruk, behovsløsere forventer at dette kalles virkningsdato
    val søknadstidspunkt = Opplysningstype.somDato("Søknadstidspunkt".id("Virkningsdato"))

    val rimeligTid = Opplysningstype.somHeltall("Rimelig tid")
    val sisteMuligeInnvilgelsesdato = Opplysningstype.somDato("Siste mulige innvilgelsesdato")
    val innenRimeligTid = Opplysningstype.somBoolsk("Søknadstidspunkt er innen rimelig tid")

    val regelsett =
        Regelsett("Søknadstidspunkt").apply {
            regel(søknadstidspunkt) { sisteAv(søknadsdato, ønsketdato) }

            // TODO: Gjør en vurdering om dette er regel eller avklaring
            regel(rimeligTid) { oppslag(søknadstidspunkt) { 14 } }
            regel(sisteMuligeInnvilgelsesdato) { leggTilDager(søknadsdato, rimeligTid) }
            regel(innenRimeligTid) { førEllerLik(søknadstidspunkt, sisteMuligeInnvilgelsesdato) }
        }
}
