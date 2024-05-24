package no.nav.dagpenger.behandling.mediator

import no.nav.dagpenger.behandling.modell.hendelser.OpplysningSvar
import no.nav.dagpenger.opplysning.Datatype
import no.nav.dagpenger.opplysning.Gyldighetsperiode
import no.nav.dagpenger.opplysning.Kilde
import no.nav.dagpenger.opplysning.Opplysningstype

class OpplysningSvarBygger<T : Comparable<T>>(
    private val type: Opplysningstype<T>,
    private val verdi: VerdiMapper,
    private val kilde: Kilde,
    private val tilstand: OpplysningSvar.Tilstand,
    private val gyldighetsperiode: Gyldighetsperiode,
) {
    companion object {
        fun String.somOpplysningstype() = Opplysningstype.finn { it.id == this }
    }

    fun opplysningSvar() =
        OpplysningSvar(
            opplysningstype = type,
            verdi = verdi.map(type.datatype),
            kilde = kilde,
            tilstand = tilstand,
            gyldighetsperiode = gyldighetsperiode,
        )

    interface VerdiMapper {
        fun <T : Comparable<T>> map(datatype: Datatype<T>): T
    }
}
