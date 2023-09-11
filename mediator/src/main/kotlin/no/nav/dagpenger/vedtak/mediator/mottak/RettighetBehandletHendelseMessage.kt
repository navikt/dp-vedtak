package no.nav.dagpenger.vedtak.mediator.mottak

import no.nav.dagpenger.vedtak.mediator.IHendelseMediator
import no.nav.dagpenger.vedtak.mediator.melding.HendelseMessage
import no.nav.dagpenger.vedtak.modell.SakId
import no.nav.dagpenger.vedtak.modell.entitet.Beløp.Companion.beløp
import no.nav.dagpenger.vedtak.modell.entitet.Dagpengeperiode
import no.nav.dagpenger.vedtak.modell.entitet.Timer.Companion.timer
import no.nav.dagpenger.vedtak.modell.hendelser.RettighetBehandletOgAvslåttHendelse
import no.nav.dagpenger.vedtak.modell.hendelser.RettighetBehandletOgInnvilgetHendelse
import no.nav.dagpenger.vedtak.modell.vedtak.rettighet.Ordinær
import no.nav.helse.rapids_rivers.JsonMessage
import no.nav.helse.rapids_rivers.MessageContext
import no.nav.helse.rapids_rivers.asLocalDate
import no.nav.helse.rapids_rivers.asLocalDateTime
import java.time.temporal.ChronoUnit
import java.util.UUID

internal class RettighetBehandletHendelseMessage(private val packet: JsonMessage) : HendelseMessage(packet) {

    override val ident: String
        get() = packet["ident"].asText()

    fun hentIdent() = this.ident

    val behandlingId: UUID
        get() = packet["behandlingId"].asUUID()

    private val sakId: SakId
        get() = packet["sakId"].asText(UUID.randomUUID().toString())

    private val hendelse
        get() = when (packet["utfall"].asText()) {
            "Innvilgelse" -> rettighetBehandletOgInnvilgetHendelse(packet, behandlingId)
            "Avslag" -> rettighetBehandletOgAvslåttHendelse(packet, behandlingId)
            "Stans" -> TODO()
            else -> throw IllegalArgumentException("Kjenner ikke utfall $this")
        }

    private fun rettighetBehandletOgAvslåttHendelse(packet: JsonMessage, behandlingId: UUID) =
        RettighetBehandletOgAvslåttHendelse(
            meldingsreferanseId = id,
            sakId = sakId,
            ident = ident,
            behandlingId = behandlingId,
            vedtakstidspunkt = packet["@opprettet"].asLocalDateTime().truncatedTo(ChronoUnit.MILLIS),
            virkningsdato = packet["Virkningsdato"].asLocalDate(),
            hovedrettighet = hovedrettighet(packet, false),
        )

    private fun rettighetBehandletOgInnvilgetHendelse(
        packet: JsonMessage,
        behandlingId: UUID,
    ) = RettighetBehandletOgInnvilgetHendelse(
        meldingsreferanseId = id,
        sakId = sakId,
        ident = ident,
        behandlingId = behandlingId,
        vedtakstidspunkt = packet["@opprettet"].asLocalDateTime().truncatedTo(ChronoUnit.MILLIS),
        virkningsdato = packet["Virkningsdato"].asLocalDate(),
        hovedrettighet = hovedrettighet(packet, true),
        dagsats = packet["Dagsats"].asDouble().beløp,
        stønadsdager = Dagpengeperiode(antallUker = packet["Periode"].asInt()).tilStønadsdager(),
        vanligArbeidstidPerDag = packet["Fastsatt vanlig arbeidstid"].asDouble().timer,
    )

    private fun hovedrettighet(packet: JsonMessage, utfall: Boolean) = when (packet["Rettighetstype"].asText()) {
        "Ordinær" -> Ordinær(utfall)
        else -> {
            throw IllegalArgumentException("Kjenner ikke rettighet '${packet["Rettighetstype"].asText()}'")
        }
    }

    override fun behandle(mediator: IHendelseMediator, context: MessageContext) {
        mediator.behandle(hendelse, this, context)
    }
}
