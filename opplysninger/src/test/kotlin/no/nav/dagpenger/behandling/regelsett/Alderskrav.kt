package no.nav.dagpenger.behandling.regelsett

import no.nav.dagpenger.behandling.Opplysningstype
import no.nav.dagpenger.behandling.Regelsett
import no.nav.dagpenger.behandling.regel.dato.førEllerLik
import no.nav.dagpenger.behandling.regel.dato.leggTilÅr
import no.nav.dagpenger.behandling.regel.dato.sisteDagIMåned
import no.nav.dagpenger.behandling.regel.oppslag
import no.nav.dagpenger.behandling.regelsett.Virkningsdato.virkningsdato
import java.time.LocalDate

object Alderskrav {
    val fødselsdato = Opplysningstype<LocalDate>("Fødselsdato")
    val aldersgrense = Opplysningstype<Int>("Aldersgrense")

    // TODO: Bør vi aliase slik? Da er det veldig tydelig hvor opplysningene kommer fra
    // val virkningsdato = Opplysningstype<LocalDate>("Virkningsdato")
    val virkningsdato = Virkningsdato.virkningsdato

    private val sisteMåned = Opplysningstype<LocalDate>("Dato søker når maks alder")
    private val sisteDagIMåned = Opplysningstype<LocalDate>("Siste mulige dag bruker kan oppfylle alderskrav")

    val vilkår = Opplysningstype<Boolean>("Oppfyller kravet til alder")

    val regelsett =
        Regelsett("alder").apply {
            oppslag(aldersgrense, virkningsdato) { 67 }
            leggTilÅr(sisteMåned, fødselsdato, aldersgrense)
            sisteDagIMåned(sisteDagIMåned, sisteMåned)
            førEllerLik(vilkår, virkningsdato, sisteDagIMåned)
        }
}
