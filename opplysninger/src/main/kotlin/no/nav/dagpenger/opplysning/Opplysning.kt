package no.nav.dagpenger.opplysning

import no.nav.dagpenger.opplysning.regel.Regel
import java.time.LocalDateTime
import java.util.UUID

data class Utledning(val regel: String, val opplysninger: List<Opplysning<*>>) {
    internal constructor(regel: Regel<*>, opplysninger: List<Opplysning<*>>) : this(regel::class.java.simpleName, opplysninger)
}

sealed class Opplysning<T : Comparable<T>>(
    val id: UUID,
    val opplysningstype: Opplysningstype<T>,
    val verdi: T,
    val gyldighetsperiode: Gyldighetsperiode,
    val utledetAv: Utledning?,
    val kilde: Kilde?,
    val opprettet: LocalDateTime,
    private val _erstattetAv: MutableList<Opplysning<T>> = mutableListOf(),
) : Klassifiserbart by opplysningstype {
    abstract fun bekreft(): Faktum<T>

    val erErstattet get() = _erstattetAv.isNotEmpty()

    val erstattetAv get() = _erstattetAv.toList()

    val kanRedigeres get() = utledetAv == null && opplysningstype.datatype != ULID

    fun overlapper(opplysning: Opplysning<*>) =
        opplysningstype == opplysning.opplysningstype && gyldighetsperiode.overlapp(opplysning.gyldighetsperiode)

    override fun equals(other: Any?) = other is Opplysning<*> && id == other.id

    override fun hashCode() = id.hashCode()

    override fun toString() = "${javaClass.simpleName} om ${opplysningstype.navn} har verdi: $verdi som er $gyldighetsperiode"

    fun erstattesAv(vararg opplysning: Opplysning<T>) {
        _erstattetAv.addAll(opplysning.toList())
    }

    abstract fun lagErstatning(opplysning: Opplysning<T>): Opplysning<T>
}

class Hypotese<T : Comparable<T>>(
    id: UUID,
    opplysningstype: Opplysningstype<T>,
    verdi: T,
    gyldighetsperiode: Gyldighetsperiode = Gyldighetsperiode(),
    utledetAv: Utledning? = null,
    kilde: Kilde? = null,
    opprettet: LocalDateTime,
) : Opplysning<T>(id, opplysningstype, verdi, gyldighetsperiode, utledetAv, kilde, opprettet) {
    constructor(
        opplysningstype: Opplysningstype<T>,
        verdi: T,
        gyldighetsperiode: Gyldighetsperiode = Gyldighetsperiode(),
        utledetAv: Utledning? = null,
        kilde: Kilde? = null,
        opprettet: LocalDateTime = LocalDateTime.now(),
    ) : this(UUIDv7.ny(), opplysningstype, verdi, gyldighetsperiode, utledetAv, kilde, opprettet)

    override fun bekreft() = Faktum(id, super.opplysningstype, verdi, gyldighetsperiode, utledetAv, kilde, opprettet)

    override fun lagErstatning(opplysning: Opplysning<T>) =
        Hypotese(
            opplysningstype,
            verdi,
            gyldighetsperiode.kopi(tom = opplysning.gyldighetsperiode.fom.minusDays(1)),
            utledetAv,
            kilde,
            opplysning.opprettet,
        )
}

class Faktum<T : Comparable<T>>(
    id: UUID,
    opplysningstype: Opplysningstype<T>,
    verdi: T,
    gyldighetsperiode: Gyldighetsperiode = Gyldighetsperiode(),
    utledetAv: Utledning? = null,
    kilde: Kilde? = null,
    opprettet: LocalDateTime,
) : Opplysning<T>(id, opplysningstype, verdi, gyldighetsperiode, utledetAv, kilde, opprettet) {
    constructor(
        opplysningstype: Opplysningstype<T>,
        verdi: T,
        gyldighetsperiode: Gyldighetsperiode = Gyldighetsperiode(),
        utledetAv: Utledning? = null,
        kilde: Kilde? = null,
        opprettet: LocalDateTime = LocalDateTime.now(),
    ) : this(UUIDv7.ny(), opplysningstype, verdi, gyldighetsperiode, utledetAv, kilde, opprettet)

    override fun bekreft() = this

    override fun lagErstatning(opplysning: Opplysning<T>) =
        Faktum(
            opplysningstype,
            verdi,
            gyldighetsperiode.kopi(tom = opplysning.gyldighetsperiode.fom.minusDays(1)),
            utledetAv,
            kilde,
            opplysning.opprettet,
        )
}
