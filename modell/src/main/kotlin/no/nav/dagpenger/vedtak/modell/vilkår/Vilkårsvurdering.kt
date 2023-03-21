package no.nav.dagpenger.vedtak.modell.vilkår

import no.nav.dagpenger.vedtak.modell.Aktivitetskontekst
import no.nav.dagpenger.vedtak.modell.SpesifikkKontekst
import no.nav.dagpenger.vedtak.modell.hendelser.Hendelse
import no.nav.dagpenger.vedtak.modell.hendelser.Rapporteringshendelse
import no.nav.dagpenger.vedtak.modell.rapportering.Dag
import java.util.UUID

abstract class Vilkårsvurdering<Vilkår : Vilkårsvurdering<Vilkår>> private constructor(
    protected val vilkårsvurderingId: UUID,
    protected var tilstand: Tilstand<Vilkår>,
) : Aktivitetskontekst {
    constructor(tilstand: Tilstand<Vilkår>) : this(UUID.randomUUID(), tilstand)

    companion object {
        fun Vilkårsvurdering<*>.vurdert() =
            this.tilstand.tilstandType != Tilstand.Type.AvventerVurdering || this.tilstand.tilstandType != Tilstand.Type.IkkeVurdert

        fun Vilkårsvurdering<*>.oppfylt() = this.tilstand.tilstandType == Tilstand.Type.Oppfylt
    }

//    open fun accept(visitor: VilkårsvurderingVisitor) {
//        visitor.visitVilkårsvurdering(vilkårsvurderingId, tilstand)
//    }

    fun håndter(rapporteringsHendelse: Rapporteringshendelse, tellendeDager: List<Dag>) {
        rapporteringsHendelse.kontekst(this)
        implementasjon { tilstand.håndter(rapporteringsHendelse, tellendeDager, this) }
    }

    internal fun endreTilstand(nyTilstand: Tilstand<Vilkår>) {
        tilstand = nyTilstand
    }

    override fun toSpesifikkKontekst(): SpesifikkKontekst =
        SpesifikkKontekst(this.javaClass.simpleName, mapOf("vilkårsvurderingId" to vilkårsvurderingId.toString()))

    protected abstract fun <T> implementasjon(block: Vilkår.() -> T): T

    sealed class Tilstand<Vilkår : Vilkårsvurdering<Vilkår>>(val tilstandType: Type) {

        enum class Type {
            Oppfylt,
            IkkeOppfylt,
            IkkeVurdert,
            AvventerVurdering,
        }

        open fun håndter(rapporteringsHendelse: Rapporteringshendelse, tellendeDager: List<Dag>, vilkårsvurdering: Vilkår) {
            feilmelding(rapporteringsHendelse)
        }

        private fun feilmelding(hendelse: Hendelse) =
            hendelse.warn("Kan ikke håndtere ${hendelse.javaClass.simpleName} i tilstand ${this.tilstandType}")

//        open fun accept(vilkår: Vilkår, visitor: VilkårsvurderingVisitor) {}

        abstract class IkkeVurdert<Vilkår : Vilkårsvurdering<Vilkår>> : Tilstand<Vilkår>(Type.IkkeVurdert)
        abstract class Avventer<Vilkår : Vilkårsvurdering<Vilkår>> : Tilstand<Vilkår>(Type.AvventerVurdering)
        abstract class Oppfylt<Vilkår : Vilkårsvurdering<Vilkår>> : Tilstand<Vilkår>(Type.Oppfylt)
        abstract class IkkeOppfylt<Vilkår : Vilkårsvurdering<Vilkår>> : Tilstand<Vilkår>(Type.IkkeOppfylt)
    }
}
