package no.nav.dagpenger.opplysning

import no.nav.dagpenger.opplysning.regel.Regel
import java.util.UUID

data class Utledning internal constructor(
    val regel: Regel<*>,
    val opplysninger: List<Opplysning<*>>,
)

data class Kilde(
    val meldingsreferanseId: UUID,
)

sealed class Opplysning<T : Comparable<T>>(
    val id: UUID,
    val opplysningstype: Opplysningstype<T>,
    val verdi: T,
    val gyldighetsperiode: Gyldighetsperiode,
    val utledetAv: Utledning?,
    val kilde: Kilde?,
) : Klassifiserbart by opplysningstype {
    abstract fun bekreft(): Faktum<T>

    override fun toString() = "${javaClass.simpleName} om ${opplysningstype.navn} har verdi: $verdi som er $gyldighetsperiode"

    fun sammeSom(opplysning: Opplysning<*>) =
        opplysningstype == opplysning.opplysningstype && gyldighetsperiode.overlapp(opplysning.gyldighetsperiode)

    override fun equals(other: Any?) = other is Opplysning<*> && id == other.id

    override fun hashCode() = id.hashCode()
}

class Hypotese<T : Comparable<T>> constructor(
    id: UUID,
    opplysningstype: Opplysningstype<T>,
    verdi: T,
    gyldighetsperiode: Gyldighetsperiode = Gyldighetsperiode(),
    utledetAv: Utledning? = null,
    kilde: Kilde? = null,
) : Opplysning<T>(id, opplysningstype, verdi, gyldighetsperiode, utledetAv, kilde) {
    constructor(
        opplysningstype: Opplysningstype<T>,
        verdi: T,
        gyldighetsperiode: Gyldighetsperiode = Gyldighetsperiode(),
        utledetAv: Utledning? = null,
        kilde: Kilde? = null,
    ) : this(UUIDv7.ny(), opplysningstype, verdi, gyldighetsperiode, utledetAv, kilde)

    override fun bekreft() = Faktum(id, super.opplysningstype, verdi, gyldighetsperiode, utledetAv)
}

class Faktum<T : Comparable<T>> constructor(
    id: UUID,
    opplysningstype: Opplysningstype<T>,
    verdi: T,
    gyldighetsperiode: Gyldighetsperiode = Gyldighetsperiode(),
    utledetAv: Utledning? = null,
    kilde: Kilde? = null,
) : Opplysning<T>(id, opplysningstype, verdi, gyldighetsperiode, utledetAv, kilde) {
    constructor(
        opplysningstype: Opplysningstype<T>,
        verdi: T,
        gyldighetsperiode: Gyldighetsperiode = Gyldighetsperiode(),
        utledetAv: Utledning? = null,
        kilde: Kilde? = null,
    ) : this(UUIDv7.ny(), opplysningstype, verdi, gyldighetsperiode, utledetAv, kilde)

    override fun bekreft() = this
}
