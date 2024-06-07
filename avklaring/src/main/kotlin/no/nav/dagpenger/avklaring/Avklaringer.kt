package no.nav.dagpenger.avklaring

import no.nav.dagpenger.opplysning.LesbarOpplysninger

class Avklaringer(private val kontrollpunkter: List<Kontrollpunkt>, avklaringer: List<Avklaring> = emptyList()) {
    internal val avklaringer = avklaringer.toMutableSet()

    fun måAvklares(opplysninger: LesbarOpplysninger): List<Avklaring> {
        val aktivteAvklaringer =
            kontrollpunkter.map { it.evaluer(opplysninger) }
                .filterIsInstance<Kontrollpunkt.Kontrollresultat.KreverAvklaring>()
                .map { it.avklaringkode }

        // Avbryt alle avklaringer som ikke lenger er aktive
        avklaringer.filter { it.måAvklares() && !aktivteAvklaringer.contains(it.kode) }.forEach { it.avbryt() }

        // Gjenåpne avklaringer som ikke er avklart og er aktive igjen
        aktivteAvklaringer.mapNotNull { avklaringskode -> avklaringer.find { it.kode == avklaringskode && !it.erAvklart() } }
            .forEach { it.gjenåpne() }

        // Legg til nye avklaringer
        avklaringer.addAll(aktivteAvklaringer.map { Avklaring(it) })

        return avklaringer.toList()
    }
}
