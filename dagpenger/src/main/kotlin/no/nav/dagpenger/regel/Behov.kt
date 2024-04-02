package no.nav.dagpenger.regel

import kotlin.reflect.KProperty

object Behov {
    val InntektId by StringConstant()
    val OpptjeningsperiodeFraOgMed by StringConstant()
    val SisteAvsluttendeKalenderMåned by StringConstant()
    val KanJobbeDeltid by StringConstant()
    val KanJobbeHvorSomHelst by StringConstant()
    val HelseTilAlleTyperJobb by StringConstant()
    val VilligTilÅBytteYrke by StringConstant()
    val RegistrertSomArbeidssøker by StringConstant()
}

class StringConstant {
    operator fun getValue(
        thisRef: Any?,
        property: KProperty<*>,
    ) = property.name
}
