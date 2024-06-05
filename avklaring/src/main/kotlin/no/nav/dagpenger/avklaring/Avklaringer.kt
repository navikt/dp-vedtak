package no.nav.dagpenger.avklaring

import no.nav.dagpenger.opplysning.LesbarOpplysninger

class Avklaringer(private val kontrollpunkter: List<Kontrollpunkt>, avklaringer: List<Avklaring> = emptyList()) {
    internal val avklaringer = avklaringer.toMutableSet()

    fun avklaringer(opplysninger: LesbarOpplysninger): List<Avklaring> {
        val aktiveAvklaringer =
            kontrollpunkter
                .map { it.evaluer(opplysninger) }
                .filterIsInstance<Kontrollpunkt.Kontrollresultat.KreverAvklaring>()
                .map { it.avklaring }

        // Avbryt alle avklaringer som ikke lenger er aktive
        avklaringer
            .filter { it.måAvklares() }
            .filterNot { avklaring: Avklaring -> aktiveAvklaringer.contains(avklaring) }
            .forEach { it.avbryt() }

        // Legg til nye avklaringer
        avklaringer.addAll(aktiveAvklaringer)

        // Gjenåpne avklaringer som er avklart
        avklaringer.filter { it.måAvklares() }.forEach { it.gjenåpne() }

        return avklaringer.toList()
    }
}
