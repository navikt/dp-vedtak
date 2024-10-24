package no.nav.dagpenger.behandling.mediator

import no.nav.dagpenger.behandling.modell.hendelser.OpplysningSvar
import no.nav.dagpenger.opplysning.Datatype
import no.nav.dagpenger.opplysning.Gyldighetsperiode
import no.nav.dagpenger.opplysning.Kilde
import no.nav.dagpenger.opplysning.Opplysningstype
import java.util.UUID

class OpplysningSvarBygger<T : Comparable<T>>(
    private val type: Opplysningstype<T>,
    private val verdi: VerdiMapper,
    private val kilde: Kilde,
    private val tilstand: OpplysningSvar.Tilstand,
    private val gyldighetsperiode: Gyldighetsperiode,
    private val utledetAv: List<UUID>,
) {
    fun opplysningSvar() =
        OpplysningSvar(
            opplysningstype = type,
            verdi = verdi.map(type.datatype),
            tilstand = tilstand,
            kilde = kilde,
            gyldighetsperiode = gyldighetsperiode,
            utledetAv = utledetAv,
        )

    interface VerdiMapper {
        fun <T : Comparable<T>> map(datatype: Datatype<T>): T
    }
}
