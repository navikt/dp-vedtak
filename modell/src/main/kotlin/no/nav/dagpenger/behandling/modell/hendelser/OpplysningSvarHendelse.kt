package no.nav.dagpenger.behandling.modell.hendelser

import no.nav.dagpenger.opplysning.Faktum
import no.nav.dagpenger.opplysning.Gyldighetsperiode
import no.nav.dagpenger.opplysning.Hypotese
import no.nav.dagpenger.opplysning.Kilde
import no.nav.dagpenger.opplysning.Opplysning
import no.nav.dagpenger.opplysning.Opplysninger
import no.nav.dagpenger.opplysning.Opplysningstype
import java.time.LocalDateTime
import java.util.UUID

abstract class NyOpplysningHendelse(
    meldingsreferanseId: UUID,
    ident: String,
    override val behandlingId: UUID,
    opprettet: LocalDateTime,
) : PersonHendelse(meldingsreferanseId, ident, opprettet),
    BehandlingHendelse {
    abstract fun leggTil(opplysninger: Opplysninger)
}

open class OpplysningSvarHendelse(
    meldingsreferanseId: UUID,
    ident: String,
    behandlingId: UUID,
    val opplysninger: List<OpplysningSvar<*>>,
    opprettet: LocalDateTime,
) : NyOpplysningHendelse(meldingsreferanseId, ident, behandlingId, opprettet) {
    init {
        require(opplysninger.isNotEmpty()) { "Må ha minst én opplysning" }
    }

    override fun leggTil(opplysninger: Opplysninger) {
        this.opplysninger.forEach { opplysning ->
            info("Mottok svar på opplysning om ${opplysning.opplysningstype}")
            opplysninger.leggTil(opplysning.opplysning())
        }
    }
}

class ErstattOpplysningHendelse<T : Comparable<T>>(
    meldingsreferanseId: UUID,
    ident: String,
    behandlingId: UUID,
    private val erstatt: UUID,
    private val erstatning: OpplysningSvar<T>,
    opprettet: LocalDateTime,
) : NyOpplysningHendelse(
        meldingsreferanseId,
        ident,
        behandlingId,
        opprettet,
    ) {
    @Suppress("UNCHECKED_CAST")
    override fun leggTil(opplysninger: Opplysninger) {
        val erstatt = opplysninger.finnOpplysning(erstatt) as Opplysning<T>
        val erstatning = erstatning.opplysning()
        require(erstatning.er(erstatt.opplysningstype)) { "Erstatning må være av samme type som erstatt" }

        info("Erstatter opplysning om ${erstatt.opplysningstype}")
        opplysninger.erstatt(erstatt, erstatning)
    }
}

data class OpplysningSvar<T : Comparable<T>>(
    val opplysningstype: Opplysningstype<T>,
    val verdi: T,
    val tilstand: Tilstand,
    val kilde: Kilde,
    val gyldighetsperiode: Gyldighetsperiode? = null,
) {
    enum class Tilstand {
        Hypotese,
        Faktum,
    }

    fun opplysning(): Opplysning<T> {
        val gyldighetsperiode = gyldighetsperiode ?: Gyldighetsperiode()
        return when (tilstand) {
            Tilstand.Hypotese -> Hypotese(opplysningstype, verdi, kilde = kilde, gyldighetsperiode = gyldighetsperiode)
            Tilstand.Faktum -> Faktum(opplysningstype, verdi, kilde = kilde, gyldighetsperiode = gyldighetsperiode)
        }
    }
}
