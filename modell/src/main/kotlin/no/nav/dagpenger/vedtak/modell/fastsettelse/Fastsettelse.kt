package no.nav.dagpenger.vedtak.modell.fastsettelse

import no.nav.dagpenger.vedtak.modell.Aktivitetskontekst
import no.nav.dagpenger.vedtak.modell.SpesifikkKontekst
import no.nav.dagpenger.vedtak.modell.hendelser.Hendelse
import no.nav.dagpenger.vedtak.modell.hendelser.Rapporteringshendelse
import no.nav.dagpenger.vedtak.modell.rapportering.Dag
import no.nav.dagpenger.vedtak.modell.visitor.FastsettelseVisitor
import java.util.UUID

internal abstract class Fastsettelse<Paragraf : Fastsettelse<Paragraf>>(
    protected val fastsettelseId: UUID,
    protected var tilstand: Tilstand<Paragraf>,
) : Aktivitetskontekst {

    constructor(tilstand: Tilstand<Paragraf>) : this(UUID.randomUUID(), tilstand)

    companion object {
        fun List<Fastsettelse<*>>.vurdert() =
            this.all { it.tilstand.tilstandType == Tilstand.Type.Vurdert }
    }

    abstract fun accept(visitor: FastsettelseVisitor)

    fun håndter(rapporteringsHendelse: Rapporteringshendelse, tellendeDager: List<Dag>) {
        rapporteringsHendelse.kontekst(this)
        implementasjon { tilstand.håndter(rapporteringsHendelse, tellendeDager, this) }
    }

    override fun toSpesifikkKontekst(): SpesifikkKontekst =
        SpesifikkKontekst(this.javaClass.simpleName)

    protected abstract fun <T> implementasjon(block: Paragraf.() -> T): T

    sealed class Tilstand<Paragraf : Fastsettelse<Paragraf>>(val tilstandType: Type) {
        open fun accept(paragraf: Paragraf, visitor: FastsettelseVisitor) {}

        open fun håndter(hendelse: Hendelse, fastsettelse: Paragraf) {
            hendelse.tilstandfeil()
        }

        open fun håndter(rapporteringsHendelse: Rapporteringshendelse, tellendeDager: List<Dag>, fastsettelse: Paragraf) {
            rapporteringsHendelse.tilstandfeil()
        }

        enum class Type {
            IkkeVurdert,
            AvventerVurdering,
            Vurdert,
        }

        abstract class IkkeVurdert<Paragraf : Fastsettelse<Paragraf>> : Tilstand<Paragraf>(Type.IkkeVurdert)
        abstract class Avventer<Paragraf : Fastsettelse<Paragraf>> : Tilstand<Paragraf>(Type.AvventerVurdering)
        abstract class Vurdert<Paragraf : Fastsettelse<Paragraf>> : Tilstand<Paragraf>(Type.Vurdert)

        private fun Hendelse.tilstandfeil() {
            this.warn("Forventet ikke ${this.javaClass.simpleName} i tilstand ${tilstandType.name} ")
        }
    }

    internal fun endreTilstand(nyTilstand: Tilstand<Paragraf>) {
        tilstand = nyTilstand
    }
}
