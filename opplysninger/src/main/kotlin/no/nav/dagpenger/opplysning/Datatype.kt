package no.nav.dagpenger.opplysning

import no.nav.dagpenger.opplysning.verdier.Beløp
import no.nav.dagpenger.opplysning.verdier.Inntekt
import no.nav.dagpenger.opplysning.verdier.Ulid
import java.time.LocalDate

sealed class Datatype<T : Comparable<T>>(
    val klasse: Class<T>,
) {
    companion object {
        fun fromString(datatype: String): Datatype<*> =
            when (datatype) {
                "Dato" -> Dato
                "Desimaltall" -> Desimaltall
                "Heltall" -> Heltall
                "Boolsk" -> Boolsk
                "ULID" -> ULID
                "Penger" -> Penger
                "Inntekt" -> InntektDataType
                else -> throw IllegalArgumentException("Unknown datatype: $datatype")
            }
    }

    open fun navn(): String = this.javaClass.simpleName
}

data object Dato : Datatype<LocalDate>(LocalDate::class.java)

data object Desimaltall : Datatype<Double>(Double::class.java)

data object Heltall : Datatype<Int>(Int::class.java)

data object Boolsk : Datatype<Boolean>(Boolean::class.java)

data object ULID : Datatype<Ulid>(Ulid::class.java)

data object Penger : Datatype<Beløp>(Beløp::class.java)

data object InntektDataType : Datatype<Inntekt>(Inntekt::class.java) {
    override fun navn(): String = "Inntekt"
}
