package no.nav.dagpenger.opplysning.regelsett

import no.nav.dagpenger.opplysning.Boolsk
import no.nav.dagpenger.opplysning.Dato
import no.nav.dagpenger.opplysning.Heltall
import no.nav.dagpenger.opplysning.Opplysningstype
import no.nav.dagpenger.opplysning.Regelsett
import no.nav.dagpenger.opplysning.regel.dato.førEllerLik
import no.nav.dagpenger.opplysning.regel.dato.leggTilÅr
import no.nav.dagpenger.opplysning.regel.dato.sisteDagIMåned
import no.nav.dagpenger.opplysning.regel.innhentes
import no.nav.dagpenger.opplysning.regel.oppslag
import no.nav.dagpenger.uuid.UUIDv7

internal object Alderskrav {
    val fødselsdato = Opplysningstype.dato(Opplysningstype.Id(UUIDv7.ny(), Dato), "Fødselsdato")
    val aldersgrense = Opplysningstype.heltall(Opplysningstype.Id(UUIDv7.ny(), Heltall), "Aldersgrense")

    private val virkningsdato = Prøvingsdato.prøvingsdato
    private val sisteMåned = Opplysningstype.somDato("Dato søker når maks alder")
    private val sisteDagIMåned = Opplysningstype.somDato("Siste mulige dag bruker kan oppfylle alderskrav")

    val vilkår = Opplysningstype.boolsk(Opplysningstype.Id(UUIDv7.ny(), Boolsk), "Oppfyller kravet til alder")

    val regelsett =
        Regelsett("alder") {
            regel(fødselsdato) { innhentes }
            regel(aldersgrense) { oppslag(virkningsdato) { 67 } }
            regel(sisteMåned) { leggTilÅr(fødselsdato, aldersgrense) }
            regel(sisteDagIMåned) { sisteDagIMåned(sisteMåned) }
            regel(vilkår) { førEllerLik(virkningsdato, sisteDagIMåned) }
        }
}
