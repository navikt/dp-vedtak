package no.nav.dagpenger.avklaring

import no.nav.dagpenger.opplysning.LesbarOpplysninger

class Avklaringer(private val kontrollpunkter: List<Kontrollpunkt>, avklaringer: List<Avklaring> = emptyList()) {
    internal val avklaringer = avklaringer.toMutableSet()

    fun måAvklares(opplysninger: LesbarOpplysninger): List<Avklaring> {
        val aktiveAvklaringer =
            kontrollpunkter
                .map { it.evaluer(opplysninger) }
                .filterIsInstance<Kontrollpunkt.Kontrollresultat.KreverAvklaring>()
                .map { it.avklaringkode }

        // Avbryt alle avklaringer som ikke lenger er aktive
        avklaringer
            .filter { it.måAvklares() }
            .filterNot { avklaring: Avklaring -> aktiveAvklaringer.contains(avklaring.kode) }
            .forEach { it.avbryt() }

        // Gjenåpne avklaringer som ikke er avklart og er aktive igjen
        aktiveAvklaringer
            .filter { avklaringskode -> avklaringer.find { it.kode == avklaringskode } != null }
            .filterNot { avklaringskode -> avklaringer.find { it.kode == avklaringskode }?.erAvklart() == true }
            .map { avklaringskode -> avklaringer.find { it.kode == avklaringskode } }.forEach { it?.gjenåpne() }

        // Legg til nye avklaringer
        avklaringer.addAll(aktiveAvklaringer.map { Avklaring(it) })

        return avklaringer.toList()
    }
}
