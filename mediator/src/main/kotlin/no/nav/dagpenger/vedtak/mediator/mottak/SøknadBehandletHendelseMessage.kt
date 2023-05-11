package no.nav.dagpenger.vedtak.mediator.mottak

import no.nav.dagpenger.vedtak.mediator.IHendelseMediator
import no.nav.dagpenger.vedtak.mediator.melding.HendelseMessage
import no.nav.dagpenger.vedtak.modell.Dagpengerettighet
import no.nav.dagpenger.vedtak.modell.entitet.Timer.Companion.timer
import no.nav.dagpenger.vedtak.modell.hendelser.DagpengerAvslåttHendelse
import no.nav.dagpenger.vedtak.modell.hendelser.DagpengerInnvilgetHendelse
import no.nav.dagpenger.vedtak.modell.mengde.Enhet.Companion.arbeidsuker
import no.nav.helse.rapids_rivers.JsonMessage
import no.nav.helse.rapids_rivers.MessageContext
import java.math.BigDecimal
import java.time.LocalDate
import java.util.UUID

internal class SøknadBehandletHendelseMessage(private val packet: JsonMessage) : HendelseMessage(packet) {

    override val ident: String
        get() = packet["ident"].asText()

    private val behandlingId = packet["behandlingId"].asUUID()

    private val hendelse get() = when (packet["innvilget"].asBoolean()) {
        true -> dagpengerInnvilgetHendelse(packet, behandlingId)
        false -> dagpengerAvslåttHendelse(packet, behandlingId)
    }

    private fun dagpengerAvslåttHendelse(packet: JsonMessage, behandlingId: UUID) =
        DagpengerAvslåttHendelse(
            ident = ident,
            behandlingId = behandlingId,
            virkningsdato = LocalDate.parse(packet["Virkningsdato"].asText()),
        )

    private fun dagpengerInnvilgetHendelse(
        packet: JsonMessage,
        behandlingId: UUID,
    ) = DagpengerInnvilgetHendelse(
        ident = ident,
        behandlingId = behandlingId,
        virkningsdato = LocalDate.parse(packet["Virkningsdato"].asText()),
        dagpengerettighet = Dagpengerettighet.valueOf(packet["Rettighetstype"].asText()),
        dagsats = packet["Dagsats"].decimalValue(),
        grunnlag = packet["Grunnlag"].decimalValue(),
        stønadsperiode = packet["Periode"].asInt().arbeidsuker,
        vanligArbeidstidPerDag = packet["Fastsatt vanlig arbeidstid"].asDouble().timer,
        egenandel = when (Dagpengerettighet.valueOf(packet["Rettighetstype"].asText())) {
            Dagpengerettighet.Ordinær, Dagpengerettighet.Permittering -> packet["Dagsats"].decimalValue() * BigDecimal(3)
            else -> BigDecimal(0)
        }, // @todo: hva/hvem skal sette egenandel?
    )

    override fun behandle(mediator: IHendelseMediator, context: MessageContext) {
        mediator.behandle(hendelse, this, context)
    }
}
