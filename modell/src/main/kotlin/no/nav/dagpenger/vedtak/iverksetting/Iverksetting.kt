package no.nav.dagpenger.vedtak.iverksetting

import no.nav.dagpenger.vedtak.iverksetting.hendelser.VedtakFattetHendelse
import no.nav.dagpenger.vedtak.modell.Aktivitetskontekst
import no.nav.dagpenger.vedtak.modell.Aktivitetslogg
import no.nav.dagpenger.vedtak.modell.SpesifikkKontekst
import no.nav.dagpenger.vedtak.modell.hendelser.Hendelse
import java.util.UUID

class Iverksetting(
    private val id: UUID,
    private val vedtakId: UUID,
    private var tilstand: Tilstand,
    internal val aktivitetslogg: Aktivitetslogg = Aktivitetslogg(),
) : Aktivitetskontekst {

    private val observers = mutableListOf<IverksettingObserver>()
    constructor(vedtakId: UUID) : this(id = UUID.randomUUID(), vedtakId = vedtakId, Mottatt)

    override fun toSpesifikkKontekst(): SpesifikkKontekst {
        return SpesifikkKontekst(
            "Iverksetting",
            mapOf("iverksettingId" to id.toString(), "vedtakId" to vedtakId.toString()),
        )
    }

    fun håndter(vedtakFattetHendelse: VedtakFattetHendelse) {
        kontekst(vedtakFattetHendelse)
        tilstand.håndter(vedtakFattetHendelse, this)
    }

    private fun kontekst(hendelse: Hendelse) {
        hendelse.kontekst(this)
        hendelse.kontekst(tilstand)
    }

    fun addObserver(iverksettingObserver: IverksettingObserver) {
        observers.add(iverksettingObserver)
    }

    private fun endreTilstand(hendelse: Hendelse, nyTilstand: Tilstand) {
        if(nyTilstand == tilstand) {
            return // vi er allerede i denne tilstanden
        }
        val forrigeTilstand = tilstand
        tilstand = nyTilstand
        hendelse.kontekst(tilstand)
        tilstand.entering(hendelse, this)
        varsleOmEndretTilstand(forrigeTilstand)
    }

    private fun varsleOmEndretTilstand(forrigeTilstand: Tilstand) {
        observers.forEach {
            it.iverksettingTilstandEndret(
                IverksettingObserver.IverksettingEndretTilstandEvent(
                    iversettingId = id,
                    vedtakId = vedtakId,
                    forrigeTilstand = forrigeTilstand.tilstandnavn,
                    gjeldendeTilstand = tilstand.tilstandnavn
                )
            )
        }
    }

    sealed class Tilstand(val tilstandnavn: TilstandNavn) : Aktivitetskontekst {

        enum class TilstandNavn {
            Mottatt,
            AvventerIverksetting,
        }

        fun entering(hendelse: Hendelse, iverksetting: Iverksetting) {}
        fun leaving(hendelse: Hendelse, iverksetting: Iverksetting) {}

        override fun toSpesifikkKontekst(): SpesifikkKontekst =
            SpesifikkKontekst("IverksettingTilstand", mapOf("tilstand" to tilstandnavn.name))

        open fun håndter(vedtakFattetHendelse: VedtakFattetHendelse, iverksetting: Iverksetting) {
            vedtakFattetHendelse.severe("Kan ikke håndtere ${vedtakFattetHendelse.javaClass.simpleName} i iverksetting-tilstand $tilstandnavn")
        }
    }

    object Mottatt : Tilstand(TilstandNavn.Mottatt) {
        override fun håndter(vedtakFattetHendelse: VedtakFattetHendelse, iverksetting: Iverksetting) {
            vedtakFattetHendelse.behov(
                type = Aktivitetslogg.Aktivitet.Behov.Behovtype.Iverksett,
                melding = "Trenger å iverksette vedtak",

            )
            iverksetting.endreTilstand(vedtakFattetHendelse, AvventerIverksetting)
        }
    }



    object AvventerIverksetting : Tilstand(TilstandNavn.AvventerIverksetting)
}
