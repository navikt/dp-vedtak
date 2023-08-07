package no.nav.dagpenger.vedtak.iverksetting

import no.nav.dagpenger.aktivitetslogg.Aktivitetskontekst
import no.nav.dagpenger.aktivitetslogg.Aktivitetslogg
import no.nav.dagpenger.aktivitetslogg.SpesifikkKontekst
import no.nav.dagpenger.aktivitetslogg.Subaktivitetskontekst
import no.nav.dagpenger.vedtak.iverksetting.hendelser.HovedrettighetVedtakFattetHendelse
import no.nav.dagpenger.vedtak.iverksetting.hendelser.IverksattHendelse
import no.nav.dagpenger.vedtak.iverksetting.hendelser.UtbetalingsvedtakFattetHendelse
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
        tilstand = Mottatt,
    )

    companion object {
        fun rehydrer(
            id: UUID,
            personIdentifikator: PersonIdentifikator,
            vedtakId: UUID,
            tilstand: Tilstand,
            aktivitetslogg: Aktivitetslogg,
        ): Iverksetting {
            return Iverksetting(
                id = id,
                personIdent = personIdentifikator,
                vedtakId = vedtakId,
                tilstand = tilstand,
                aktivitetslogg = aktivitetslogg,
            )
        }
    }

    fun accept(iverksettingVisitor: IverksettingVisitor) {
        iverksettingVisitor.visitIverksetting(id, vedtakId, personIdent, tilstand)
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

    fun håndter(utbetalingsvedtakFattetHendelse: UtbetalingsvedtakFattetHendelse) {
        kontekst(utbetalingsvedtakFattetHendelse)
        tilstand.håndter(utbetalingsvedtakFattetHendelse, this)
    }

    fun håndter(hovedrettighetVedtakFattetHendelse: HovedrettighetVedtakFattetHendelse) {
        kontekst(hovedrettighetVedtakFattetHendelse)
        tilstand.håndter(hovedrettighetVedtakFattetHendelse, this)
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
                    forrigeTilstand = forrigeTilstand.tilstandNavn,
                    gjeldendeTilstand = tilstand.tilstandNavn,
                ),
            )
        }
    }

    sealed class Tilstand(val tilstandNavn: TilstandNavn) : Aktivitetskontekst {

        enum class TilstandNavn {
            Mottatt,
            AvventerIverksetting,
            Iverksatt,
        }

        open fun entering(hendelse: Hendelse, iverksetting: Iverksetting) {}
        open fun leaving(hendelse: Hendelse, iverksetting: Iverksetting) {}

        override fun toSpesifikkKontekst(): SpesifikkKontekst =
            SpesifikkKontekst("IverksettingTilstand", mapOf("tilstand" to tilstandNavn.name))

        open fun håndter(utbetalingsvedtakFattetHendelse: UtbetalingsvedtakFattetHendelse, iverksetting: Iverksetting) {
            utbetalingsvedtakFattetHendelse.feiltilstand()
        }

        open fun håndter(iverksattHendelse: IverksattHendelse, iverksetting: Iverksetting) {
            iverksattHendelse.feiltilstand()
        }

        open fun håndter(hovedrettighetVedtakFattetHendelse: HovedrettighetVedtakFattetHendelse, iverksetting: Iverksetting) {
            hovedrettighetVedtakFattetHendelse.feiltilstand()
        }

        private fun Hendelse.feiltilstand(): Nothing =
            this.logiskFeil("Kan ikke håndtere ${this.javaClass.simpleName} i iverksetting-tilstand $tilstandNavn")
    }

    object Mottatt : Tilstand(TilstandNavn.Mottatt) {
        override fun håndter(utbetalingsvedtakFattetHendelse: UtbetalingsvedtakFattetHendelse, iverksetting: Iverksetting) {
            utbetalingsvedtakFattetHendelse.behov(
                type = IverksettingBehov.Iverksett,
                melding = "Sender behov for å iverksette utbetalingsvedtak",
                detaljer = mapOf(
                    "vedtakId" to utbetalingsvedtakFattetHendelse.vedtakId,
                    "behandlingId" to utbetalingsvedtakFattetHendelse.behandlingId,
                    "vedtakstidspunkt" to utbetalingsvedtakFattetHendelse.vedtakstidspunkt,
                    "virkningsdato" to utbetalingsvedtakFattetHendelse.virkningsdato,
                    "utbetalingsdager" to utbetalingsvedtakFattetHendelse.utbetalingsdager,
                    "utfall" to utbetalingsvedtakFattetHendelse.utfall.name,
                ),
            )
            iverksetting.endreTilstand(utbetalingsvedtakFattetHendelse, AvventerIverksetting)
        }

        override fun håndter(
            hovedrettighetVedtakFattetHendelse: HovedrettighetVedtakFattetHendelse,
            iverksetting: Iverksetting,
        ) {
            hovedrettighetVedtakFattetHendelse.behov(
                type = IverksettingBehov.Iverksett,
                melding = "Sender behov for å iverksette vedtak med hovedrettighet",
                detaljer = mapOf(
                    "vedtakId" to hovedrettighetVedtakFattetHendelse.vedtakId,
                    "behandlingId" to hovedrettighetVedtakFattetHendelse.behandlingId,
                    "vedtakstidspunkt" to hovedrettighetVedtakFattetHendelse.vedtakstidspunkt,
                    "virkningsdato" to hovedrettighetVedtakFattetHendelse.virkningsdato,
                    "utfall" to hovedrettighetVedtakFattetHendelse.utfall.name,
                ),
            )
            iverksetting.endreTilstand(hovedrettighetVedtakFattetHendelse, AvventerIverksetting)
        }
    }

    object AvventerIverksetting : Tilstand(TilstandNavn.AvventerIverksetting) {

        override fun entering(hendelse: Hendelse, iverksetting: Iverksetting) {
            hendelse.info("Avventer iverksetting")
        }
        override fun håndter(iverksattHendelse: IverksattHendelse, iverksetting: Iverksetting) {
            iverksetting.endreTilstand(iverksattHendelse, Iverksatt)
        }
    }

    object Iverksatt : Tilstand(TilstandNavn.Iverksatt) {
        override fun entering(hendelse: Hendelse, iverksetting: Iverksetting) {
            hendelse.info("Vedtak er iverksatt")
        }
    }
}
