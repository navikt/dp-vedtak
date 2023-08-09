package no.nav.dagpenger.vedtak.mediator.mottak

import no.nav.dagpenger.vedtak.mediator.IHendelseMediator
import no.nav.dagpenger.vedtak.mediator.melding.HendelseMessage
import no.nav.dagpenger.vedtak.modell.Dagpengerettighet
import no.nav.dagpenger.vedtak.modell.entitet.Beløp.Companion.beløp
import no.nav.dagpenger.vedtak.modell.entitet.Dagpengeperiode
import no.nav.dagpenger.vedtak.modell.entitet.Timer.Companion.timer
import no.nav.dagpenger.vedtak.modell.hendelser.DagpengerAvslåttHendelse
import no.nav.dagpenger.vedtak.modell.hendelser.DagpengerInnvilgetHendelse
import no.nav.helse.rapids_rivers.JsonMessage
import no.nav.helse.rapids_rivers.MessageContext
import no.nav.helse.rapids_rivers.asLocalDate
import no.nav.helse.rapids_rivers.asLocalDateTime
import java.time.temporal.ChronoUnit
import java.util.UUID

internal class SøknadBehandletHendelseMessage(private val packet: JsonMessage) : HendelseMessage(packet) {

    override val ident: String
        get() = packet["ident"].asText()

    fun hentIdent() = this.ident

    val behandlingId: UUID
        get() = packet["behandlingId"].asUUID()

    private val hendelse
        get() = when (packet["innvilget"].asBoolean()) {
            true -> dagpengerInnvilgetHendelse(packet, behandlingId)
            false -> dagpengerAvslåttHendelse(packet, behandlingId)
        }

    private fun dagpengerAvslåttHendelse(packet: JsonMessage, behandlingId: UUID) =
        DagpengerAvslåttHendelse(
            meldingsreferanseId = id,
            sakId = packet["sak_id"].asText("SAK_NUMMER_1"),
            ident = ident,
            behandlingId = behandlingId,
            vedtakstidspunkt = packet["@opprettet"].asLocalDateTime().truncatedTo(ChronoUnit.MILLIS),
            virkningsdato = packet["Virkningsdato"].asLocalDate(),
            dagpengerettighet = Dagpengerettighet.valueOf(packet["Rettighetstype"].asText()),
        )

    private fun dagpengerInnvilgetHendelse(
        packet: JsonMessage,
        behandlingId: UUID,
    ) = DagpengerInnvilgetHendelse(
        meldingsreferanseId = id,
        sakId = packet["sak_id"].asText("SAK_NUMMER_1"),
        ident = ident,
        behandlingId = behandlingId,
        vedtakstidspunkt = packet["@opprettet"].asLocalDateTime().truncatedTo(ChronoUnit.MILLIS),
        virkningsdato = packet["Virkningsdato"].asLocalDate(),
        dagpengerettighet = Dagpengerettighet.valueOf(packet["Rettighetstype"].asText()),
        dagsats = packet["Dagsats"].asDouble().beløp,
        stønadsdager = Dagpengeperiode(antallUker = packet["Periode"].asInt()).tilStønadsdager(),
        vanligArbeidstidPerDag = packet["Fastsatt vanlig arbeidstid"].asDouble().timer,
        egenandel = when (Dagpengerettighet.valueOf(packet["Rettighetstype"].asText())) {
            Dagpengerettighet.Ordinær, Dagpengerettighet.Permittering -> 3.beløp * packet["Dagsats"].asText().toBigDecimal().beløp
            else -> 0.beløp
        }, // @todo: hva/hvem skal sette egenandel?
    )

    override fun behandle(mediator: IHendelseMediator, context: MessageContext) {
        mediator.behandle(hendelse, this, context)
    }
}
