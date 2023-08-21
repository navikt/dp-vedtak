package no.nav.dagpenger.vedtak.mediator.mottak

import no.nav.dagpenger.vedtak.mediator.IHendelseMediator
import no.nav.dagpenger.vedtak.mediator.melding.HendelseMessage
import no.nav.dagpenger.vedtak.modell.SakId
import no.nav.dagpenger.vedtak.modell.entitet.Beløp.Companion.beløp
import no.nav.dagpenger.vedtak.modell.entitet.Dagpengeperiode
import no.nav.dagpenger.vedtak.modell.entitet.Timer.Companion.timer
import no.nav.dagpenger.vedtak.modell.hendelser.DagpengerAvslåttHendelse
import no.nav.dagpenger.vedtak.modell.hendelser.DagpengerInnvilgetHendelse
import no.nav.dagpenger.vedtak.modell.vedtak.rettighet.Ordinær
import no.nav.dagpenger.vedtak.modell.vedtak.rettighet.Permittering
import no.nav.dagpenger.vedtak.modell.vedtak.rettighet.PermitteringFraFiskeindustrien
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

    private val sakId: SakId
        get() = packet["sakId"].asText(UUID.randomUUID().toString())

    private val hendelse
        get() = when (packet["innvilget"].asBoolean()) {
            true -> dagpengerInnvilgetHendelse(packet, behandlingId)
            false -> dagpengerAvslåttHendelse(packet, behandlingId)
        }

    private fun dagpengerAvslåttHendelse(packet: JsonMessage, behandlingId: UUID) =
        DagpengerAvslåttHendelse(
            meldingsreferanseId = id,
            sakId = sakId,
            ident = ident,
            behandlingId = behandlingId,
            vedtakstidspunkt = packet["@opprettet"].asLocalDateTime().truncatedTo(ChronoUnit.MILLIS),
            virkningsdato = packet["Virkningsdato"].asLocalDate(),
            hovedrettighet = when (packet["Rettighetstype"].asText()) {
                "Ordinær" -> Ordinær(false)
                "Permittering" -> Permittering(false)
                "PermitteringFraFiskeindustrien" -> PermitteringFraFiskeindustrien(false)
                else -> {
                    throw IllegalArgumentException("Kjenner ikke rettighet $this")
                }
            },
        )

    private fun dagpengerInnvilgetHendelse(
        packet: JsonMessage,
        behandlingId: UUID,
    ) = DagpengerInnvilgetHendelse(
        meldingsreferanseId = id,
        sakId = sakId,
        ident = ident,
        behandlingId = behandlingId,
        vedtakstidspunkt = packet["@opprettet"].asLocalDateTime().truncatedTo(ChronoUnit.MILLIS),
        virkningsdato = packet["Virkningsdato"].asLocalDate(),
        hovedrettighet = when (packet["Rettighetstype"].asText()) {
            "Ordinær" -> Ordinær(true)
            "Permittering" -> Permittering(true)
            "PermitteringFraFiskeindustrien" -> PermitteringFraFiskeindustrien(true)
            else -> {
                throw IllegalArgumentException("Kjenner ikke rettighet $this")
            }
        },
        dagsats = packet["Dagsats"].asDouble().beløp,
        stønadsdager = Dagpengeperiode(antallUker = packet["Periode"].asInt()).tilStønadsdager(),
        vanligArbeidstidPerDag = packet["Fastsatt vanlig arbeidstid"].asDouble().timer,
    )

    override fun behandle(mediator: IHendelseMediator, context: MessageContext) {
        mediator.behandle(hendelse, this, context)
    }
}
