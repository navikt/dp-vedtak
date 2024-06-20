package no.nav.dagpenger.avklaring

import no.nav.dagpenger.avklaring.Kontrollpunkt.Kontrollresultat.KreverAvklaring
import no.nav.dagpenger.opplysning.LesbarOpplysninger
import java.util.UUID

class Avklaringer(
    private val kontrollpunkter: List<Kontrollpunkt>,
    avklaringer: List<Avklaring> = emptyList(),
) {
    internal val avklaringer = avklaringer.toMutableSet()

    fun måAvklares(opplysninger: LesbarOpplysninger): List<Avklaring> {
        val aktiveAvklaringer =
            kontrollpunkter
                .map { it.evaluer(opplysninger) }
                .filterIsInstance<KreverAvklaring>()
                .map { it.avklaringkode }

        // Avbryt alle avklaringer som ikke lenger er aktive
        avklaringer.filter { it.måAvklares() && !aktiveAvklaringer.contains(it.kode) }.forEach { it.avbryt() }

        // Gjenåpne avklaringer som ikke er avklart og er aktive igjen
        aktiveAvklaringer
            .mapNotNull { avklaringskode -> avklaringer.find { it.kode == avklaringskode && !it.erAvklart() } }
            .forEach { it.gjenåpne() }

        // Legg til nye avklaringer
        avklaringer.addAll(aktiveAvklaringer.map { Avklaring(it) })

        return avklaringer.toList()
    }

    fun avbryt(avklaringId: UUID): Boolean = avklaringer.find { it.id == avklaringId }?.avbryt() ?: false
}
