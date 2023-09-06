package no.nav.dagpenger.vedtak.modell.vedtak

import no.nav.dagpenger.vedtak.modell.SakId
import no.nav.dagpenger.vedtak.modell.entitet.Beløp
import no.nav.dagpenger.vedtak.modell.entitet.Stønadsdager
import no.nav.dagpenger.vedtak.modell.utbetaling.Utbetalingsdag
import no.nav.dagpenger.vedtak.modell.vedtak.VedtakObserver.UtbetalingsdagDto
import no.nav.dagpenger.vedtak.modell.vedtak.VedtakObserver.UtbetalingsvedtakFattet
import no.nav.dagpenger.vedtak.modell.vedtak.VedtakObserver.Utfall.Avslått
import no.nav.dagpenger.vedtak.modell.vedtak.VedtakObserver.Utfall.Innvilget
import no.nav.dagpenger.vedtak.modell.vedtak.VedtakObserver.VedtakFattet
import no.nav.dagpenger.vedtak.modell.vedtak.rettighet.Ordinær
import no.nav.dagpenger.vedtak.modell.vedtak.rettighet.Permittering
import no.nav.dagpenger.vedtak.modell.vedtak.rettighet.PermitteringFraFiskeindustrien
import no.nav.dagpenger.vedtak.modell.visitor.VedtakVisitor
import java.time.LocalDate
import java.time.LocalDateTime
import java.util.UUID

internal class VedtakFattetVisitor : VedtakVisitor {

    lateinit var vedtakFattet: VedtakFattet
    lateinit var utbetalingsvedtakFattet: UtbetalingsvedtakFattet
    private var vedtakId: UUID? = null
    private var sakId: SakId? = null
    private var utfall: Boolean? = null

    private var behandlingId: UUID? = null
    private var virkningsdato: LocalDate? = null
    private var vedtakstidspunkt: LocalDateTime? = null

    private fun vedtakId() = requireNotNull(vedtakId) { " Forventet at vedtakId er satt. Har du husket preVisitVedtak?" }
    private fun utfall() = requireNotNull(utfall) { " Forventet at utfall er satt. Har du husket preVisitVedtak?" }

    override fun preVisitVedtak(
        vedtakId: UUID,
        sakId: SakId,
        behandlingId: UUID,
        virkningsdato: LocalDate,
        vedtakstidspunkt: LocalDateTime,
        type: Vedtak.VedtakType,
    ) {
        this.vedtakId = vedtakId
        this.sakId = sakId
        this.behandlingId = behandlingId
        this.virkningsdato = virkningsdato
        this.vedtakstidspunkt = vedtakstidspunkt
    }

    override fun visitOrdinær(ordinær: Ordinær) {
        this.utfall = ordinær.utfall
    }

    override fun visitPermitteringFraFiskeindustrien(permitteringFraFiskeindustrien: PermitteringFraFiskeindustrien) {
        this.utfall = permitteringFraFiskeindustrien.utfall
    }

    override fun visitPermittering(permittering: Permittering) {
        this.utfall = permittering.utfall
    }

    override fun postVisitVedtak(
        vedtakId: UUID,
        sakId: SakId,
        behandlingId: UUID,
        virkningsdato: LocalDate,
        vedtakstidspunkt: LocalDateTime,
        type: Vedtak.VedtakType,
    ) {
        if (!this::utbetalingsvedtakFattet.isInitialized) {
            vedtakFattet = VedtakFattet(
                vedtakId = vedtakId(),
                sakId = sakId,
                vedtakstidspunkt = vedtakstidspunkt,
                behandlingId = behandlingId,
                virkningsdato = virkningsdato,
                utfall = when (utfall()) {
                    true -> Innvilget
                    false -> Avslått
                },
            )
        }
        this.vedtakId = null
        this.utfall = null
        this.virkningsdato = null
        this.behandlingId = null
        this.vedtakstidspunkt = null
    }

    override fun visitUtbetalingsvedtak(
        vedtakId: UUID,
        utfall: Boolean,
        forbruk: Stønadsdager,
        beløpTilUtbetaling: Beløp,
        utbetalingsdager: List<Utbetalingsdag>,
    ) {
        utbetalingsvedtakFattet = UtbetalingsvedtakFattet(
            vedtakId = this.vedtakId(),
            sakId = this.sakId!!,
            behandlingId = this.behandlingId!!,
            vedtakstidspunkt = this.vedtakstidspunkt!!,
            virkningsdato = this.virkningsdato!!,
            utbetalingsdager = utbetalingsdager.map { utbetalingsdag ->
                UtbetalingsdagDto(
                    dato = utbetalingsdag.dato,
                    beløp = utbetalingsdag.beløp.reflection { it }.toDouble(),
                )
            },
            utfall = when (utfall == true) {
                true -> Innvilget
                false -> Avslått
            },
        )
    }

    override fun visitAvslag(
        vedtakId: UUID,
        sakId: SakId,
        behandlingId: UUID,
        vedtakstidspunkt: LocalDateTime,
        utfall: Boolean,
        virkningsdato: LocalDate,
    ) {
        vedtakFattet = VedtakFattet(
            vedtakId = vedtakId,
            sakId = sakId,
            vedtakstidspunkt = vedtakstidspunkt,
            behandlingId = behandlingId,
            virkningsdato = virkningsdato,
            utfall = when (utfall == true) {
                true -> Innvilget
                false -> Avslått
            },
        )
    }

    override fun visitStans(
        vedtakId: UUID,
        sakId: SakId,
        behandlingId: UUID,
        virkningsdato: LocalDate,
        vedtakstidspunkt: LocalDateTime,
        utfall: Boolean?,
    ) {
        vedtakFattet = VedtakFattet(
            vedtakId = vedtakId,
            sakId = sakId,
            vedtakstidspunkt = vedtakstidspunkt,
            behandlingId = behandlingId,
            virkningsdato = virkningsdato,
            utfall = when (utfall == true) {
                true -> Innvilget
                false -> Avslått
            },
        )
    }
}
