package no.nav.dagpenger.avklaring

import no.nav.dagpenger.opplysning.LesbarOpplysninger

fun interface Kontrollpunkt {
    fun evaluer(opplysninger: LesbarOpplysninger): Kontrollresultat

    sealed class Kontrollresultat {
        data object OK : Kontrollresultat()

        data class KreverAvklaring(val avklaringkode: Avklaringkode) : Kontrollresultat() {
            val avklaring = Avklaring(avklaringkode)
        }
    }
}
