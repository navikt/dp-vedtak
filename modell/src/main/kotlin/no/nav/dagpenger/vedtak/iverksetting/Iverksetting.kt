package no.nav.dagpenger.vedtak.iverksetting

import no.nav.dagpenger.aktivitetslogg.Aktivitetskontekst
import no.nav.dagpenger.aktivitetslogg.Aktivitetslogg
import no.nav.dagpenger.aktivitetslogg.SpesifikkKontekst
import no.nav.dagpenger.aktivitetslogg.Subaktivitetskontekst
import no.nav.dagpenger.vedtak.iverksetting.hendelser.IverksattHendelse
import no.nav.dagpenger.vedtak.iverksetting.hendelser.VedtakFattetHendelse
import no.nav.dagpenger.vedtak.modell.PersonIdentifikator
import no.nav.dagpenger.vedtak.modell.PersonIdentifikator.Companion.tilPersonIdentfikator
import no.nav.dagpenger.vedtak.modell.hendelser.Hendelse
import java.util.UUID

class Iverksetting private constructor(
    val id: UUID,
    private val personIdent: PersonIdentifikator,
    val vedtakId: UUID,
    private var tilstand: Tilstand,
    override val aktivitetslogg: Aktivitetslogg = Aktivitetslogg(),
) : Subaktivitetskontekst {

    private val observers = mutableSetOf<IverksettingObserver>()
    constructor(vedtakId: UUID, ident: String) : this(
        id = UUID.randomUUID(),
        personIdent = ident.tilPersonIdentfikator(),
        vedtakId = vedtakId,
        Mottatt,
    )

    fun accept(iverksettingVisitor: IverksettingVisitor) {
        iverksettingVisitor.visitIverksetting(id, vedtakId, tilstand)
        aktivitetslogg.accept(iverksettingVisitor)
    }

    override fun toSpesifikkKontekst(): SpesifikkKontekst {
        return SpesifikkKontekst(
            "Iverksetting",
            mapOf(
                "iverksettingId" to id.toString(),
                "vedtakId" to vedtakId.toString(),
                "ident" to personIdent.identifikator(),
            ),
        )
    }

    fun håndter(vedtakFattetHendelse: VedtakFattetHendelse) {
        kontekst(vedtakFattetHendelse)
        tilstand.håndter(vedtakFattetHendelse, this)
    }

    fun håndter(iverksattHendelse: IverksattHendelse) {
        kontekst(iverksattHendelse)
        tilstand.håndter(iverksattHendelse, this)
    }

    private fun kontekst(hendelse: Hendelse) {
        hendelse.kontekst(this)
        hendelse.kontekst(tilstand)
    }

    fun addObserver(iverksettingObserver: IverksettingObserver) {
        observers.add(iverksettingObserver)
    }

    private fun endreTilstand(hendelse: Hendelse, nyTilstand: Tilstand) {
        if (nyTilstand == tilstand) {
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
                    gjeldendeTilstand = tilstand.tilstandnavn,
                ),
            )
        }
    }

    sealed class Tilstand(val tilstandnavn: TilstandNavn) : Aktivitetskontekst {

        enum class TilstandNavn {
            Mottatt,
            AvventerIverksetting,
            Iverksatt,
        }

        fun entering(hendelse: Hendelse, iverksetting: Iverksetting) {}
        fun leaving(hendelse: Hendelse, iverksetting: Iverksetting) {}

        override fun toSpesifikkKontekst(): SpesifikkKontekst =
            SpesifikkKontekst("IverksettingTilstand", mapOf("tilstand" to tilstandnavn.name))

        open fun håndter(vedtakFattetHendelse: VedtakFattetHendelse, iverksetting: Iverksetting) {
            vedtakFattetHendelse.feiltilstand()
        }

        open fun håndter(iverksattHendelse: IverksattHendelse, iverksetting: Iverksetting) {
            iverksattHendelse.feiltilstand()
        }

        private fun Hendelse.feiltilstand() =
            this.severe("Kan ikke håndtere ${this.javaClass.simpleName} i iverksetting-tilstand $tilstandnavn")
    }

    object Mottatt : Tilstand(TilstandNavn.Mottatt) {
        override fun håndter(vedtakFattetHendelse: VedtakFattetHendelse, iverksetting: Iverksetting) {
            vedtakFattetHendelse.behov(
                type = IverksettingBehov.Iverksett,
                melding = "Trenger å iverksette vedtak",
                detaljer = mapOf(
                    "vedtakId" to vedtakFattetHendelse.iverksettingsVedtak.vedtakId,
                    "behandlingId" to vedtakFattetHendelse.iverksettingsVedtak.behandlingId,
                    "vedtakstidspunkt" to vedtakFattetHendelse.iverksettingsVedtak.vedtakstidspunkt,
                    "virkningsdato" to vedtakFattetHendelse.iverksettingsVedtak.virkningsdato,
                    "utbetalingsdager" to vedtakFattetHendelse.iverksettingsVedtak.utbetalingsdager,
                    "utfall" to vedtakFattetHendelse.iverksettingsVedtak.utfall,
                ),
            )
            iverksetting.endreTilstand(vedtakFattetHendelse, AvventerIverksetting)
        }
    }

    object AvventerIverksetting : Tilstand(TilstandNavn.AvventerIverksetting) {
        override fun håndter(iverksattHendelse: IverksattHendelse, iverksetting: Iverksetting) {
            iverksetting.endreTilstand(iverksattHendelse, Iverksatt)
        }
    }

    object Iverksatt : Tilstand(TilstandNavn.Iverksatt)
}
