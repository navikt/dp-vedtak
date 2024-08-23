package no.nav.dagpenger.opplysning

fun interface Redigerbar {
    fun kanRedigere(opplysning: Opplysning<*>): Boolean
}
