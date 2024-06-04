package no.nav.dagpenger.avklaring

import no.nav.dagpenger.opplysning.LesbarOpplysninger

fun interface Kontroll {
    fun kjør(opplysninger: LesbarOpplysninger): Boolean
}

class Kontrollpunkt(private val sjekker: Avklaringkode, private val kontroll: Kontroll) {
    fun evaluer(opplysninger: LesbarOpplysninger) =
        when {
            kontroll.kjør(opplysninger) -> Kontrollresultat.OK
            else -> Kontrollresultat.KreverAvklaring(sjekker)
        }

    sealed class Kontrollresultat {
        data object OK : Kontrollresultat()

        data class KreverAvklaring(val avklaringkode: Avklaringkode) : Kontrollresultat() {
            val avklaring = Avklaring(avklaringkode)
        }
    }
}
