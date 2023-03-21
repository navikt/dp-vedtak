package no.nav.dagpenger.vedtak.modell

import no.nav.dagpenger.vedtak.modell.fastsettelse.Fastsettelse
import no.nav.dagpenger.vedtak.modell.hendelser.Hendelse
import no.nav.dagpenger.vedtak.modell.hendelser.Rapporteringshendelse
import no.nav.dagpenger.vedtak.modell.vilkår.Vilkårsvurdering
import java.util.UUID

abstract class Behandling<Behandlingstype : Behandling<Behandlingstype>>(
    private val person: Person,
    private val behandlingsId: UUID,
    protected val hendelseId: UUID,
    protected var tilstand: Tilstand<Behandlingstype>,
    protected val vilkårsvurdering: Vilkårsvurdering<*>,
    internal val aktivitetslogg: Aktivitetslogg = Aktivitetslogg(),
) : Aktivitetskontekst {

//    private val observers = mutableListOf<BehandlingObserver>()
    internal abstract val fastsettelser: List<Fastsettelse<*>>

//    fun addObserver(observer: BehandlingObserver) {
//        observers.add(observer)
//    }
//
//    fun accept(visitor: BehandlingVisitor) {
//        visitor.preVisit(this, behandlingsId, hendelseId)
//        visitor.visitTilstand(tilstand.type)
//        vilkårsvurdering.accept(visitor)
//        aktivitetslogg.accept(visitor)
//        visitor.postVisit(this, behandlingsId, hendelseId)
//    }

    protected fun kontekst(hendelse: Hendelse, melding: String? = null) {
        hendelse.kontekst(this)
        melding?.let {
            hendelse.info(it)
        }
    }

    private fun kanIkkeHåndtere(hendelse: Hendelse) {
        hendelse.severe("${this.javaClass.simpleName} vet ikke hvordan vi skal behandle ${hendelse.javaClass.simpleName}")
    }

    companion object {
        fun List<Behandling<*>>.harHendelseId(hendelseId: UUID) =
            this.any { it.hendelseId == hendelseId }

        const val kontekstType = "Behandling"
    }

    protected abstract fun <T> implementasjon(block: Behandlingstype.() -> T): T
    protected fun endreTilstand(nyTilstand: Tilstand<Behandlingstype>, søknadHendelse: Hendelse) {
        if (nyTilstand == tilstand) {
            return // Vi er allerede i tilstanden
        }
        val forrigeTilstand = tilstand
        tilstand = nyTilstand
        søknadHendelse.kontekst(tilstand)
        implementasjon { tilstand.entering(søknadHendelse, this) }
//        emitTilstandEndret(forrigeTilstand, nyTilstand)
    }

//    private fun emitTilstandEndret(forrigeTilstand: Tilstand<Behandlingstype>, nyTilstand: Tilstand<Behandlingstype>) {
//        observers.forEach { observer ->
//            observer.behandlingTilstandEndret(
//                BehandlingObserver.BehandlingEndretTilstandEvent(
//                    behandlingsId = this.behandlingsId,
//                    ident = person.ident(),
//                    gjeldendeTilstand = nyTilstand.type,
//                    forrigeTilstand = forrigeTilstand.type,
//                ),
//            )
//        }
//    }

    sealed class Tilstand<Behandlingstype : Behandling<Behandlingstype>>(val type: Type) : Aktivitetskontekst {

        enum class Type {
            ForberedBehandling,
            VurdererVilkår,
            VurdererUtfall,
            Fastsetter,
            Kvalitetssikrer,
            Behandlet,
        }

        override fun toSpesifikkKontekst() =
            SpesifikkKontekst(
                kontekstType = "Tilstand",
                mapOf(
                    "type" to type.name,
                ),
            )

        open fun entering(hendelse: Hendelse, behandling: Behandlingstype) {}

        open fun håndter(rapporteringsHendelse: Rapporteringshendelse, behandling: Behandlingstype) {
            rapporteringsHendelse.tilstandfeil()
        }

        abstract class ForberedBehandling<Behandlingstype : Behandling<Behandlingstype>> : Tilstand<Behandlingstype>(Type.ForberedBehandling)
        abstract class VurdererVilkår<Behandlingstype : Behandling<Behandlingstype>> : Tilstand<Behandlingstype>(Type.VurdererVilkår)
        abstract class VurderUtfall<Behandlingstype : Behandling<Behandlingstype>> : Tilstand<Behandlingstype>(Type.VurdererUtfall)
        abstract class Fastsetter<Behandlingstype : Behandling<Behandlingstype>> : Tilstand<Behandlingstype>(Type.Fastsetter)
        abstract class Kvalitetssikrer<Behandlingstype : Behandling<Behandlingstype>> : Tilstand<Behandlingstype>(Type.Kvalitetssikrer)
        abstract class Behandlet<Behandlingstype : Behandling<Behandlingstype>> : Tilstand<Behandlingstype>(Type.Behandlet)

        private fun Hendelse.tilstandfeil() {
            this.warn("Forventet ikke ${this.javaClass.simpleName} i tilstand ${type.name} ")
        }
    }
}
