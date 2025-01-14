package no.nav.dagpenger.avklaring

import no.nav.dagpenger.opplysning.Avklaringkode
import no.nav.dagpenger.opplysning.LesbarOpplysninger
import no.nav.dagpenger.opplysning.LesbarOpplysningerMedLogg
import java.time.LocalDateTime

fun interface Kontroll {
    fun kjør(opplysninger: LesbarOpplysninger): Boolean
}

class Kontrollpunkt(
    private val sjekker: Avklaringkode,
    private val kontroll: Kontroll,
) {
    fun evaluer(opplysninger: LesbarOpplysninger): Kontrollresultat {
        val opplysningerMedLogg = LesbarOpplysningerMedLogg(opplysninger)
        return when {
            kontroll.kjør(opplysningerMedLogg) -> Kontrollresultat.KreverAvklaring(sjekker, opplysningerMedLogg.sistBrukteOpplysning)
            else -> Kontrollresultat.OK
        }
    }

    sealed class Kontrollresultat {
        data object OK : Kontrollresultat()

        data class KreverAvklaring(
            val avklaringkode: Avklaringkode,
            val sisteOpplysning: LocalDateTime,
        ) : Kontrollresultat()
    }
}
