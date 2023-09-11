package no.nav.dagpenger.vedtak.modell.hendelser

import no.nav.dagpenger.aktivitetslogg.Aktivitetslogg
import no.nav.dagpenger.vedtak.modell.entitet.Beløp
import no.nav.dagpenger.vedtak.modell.entitet.Stønadsdager
import no.nav.dagpenger.vedtak.modell.entitet.Timer
import no.nav.dagpenger.vedtak.modell.vedtak.Avslag.Companion.avslag
import no.nav.dagpenger.vedtak.modell.vedtak.Rammevedtak.Companion.innvilgelse
import no.nav.dagpenger.vedtak.modell.vedtak.Vedtak
import no.nav.dagpenger.vedtak.modell.vedtak.rettighet.Hovedrettighet
import java.time.LocalDate
import java.time.LocalDateTime
import java.util.UUID

sealed class RettighetBehandletHendelse(
    meldingsreferanseId: UUID,
    internal val sakId: String,
    protected val ident: String,
    internal val behandlingId: UUID,
    protected val vedtakstidspunkt: LocalDateTime,
    protected val virkningsdato: LocalDate,
    aktivitetslogg: Aktivitetslogg = Aktivitetslogg(),
) : Hendelse(meldingsreferanseId, ident, aktivitetslogg) {
    abstract fun tilVedtak(): Vedtak
}

class RettighetBehandletOgInnvilgetHendelse(
    meldingsreferanseId: UUID,
    sakId: String,
    ident: String,
    behandlingId: UUID,
    vedtakstidspunkt: LocalDateTime,
    virkningsdato: LocalDate,
    private val hovedrettighet: Hovedrettighet,
    private val dagsats: Beløp,
    private val stønadsdager: Stønadsdager,
    private val vanligArbeidstidPerDag: Timer,
) : RettighetBehandletHendelse(
    meldingsreferanseId = meldingsreferanseId,
    sakId = sakId,
    ident = ident,
    behandlingId = behandlingId,
    vedtakstidspunkt = vedtakstidspunkt,
    virkningsdato = virkningsdato,
) {
    override fun tilVedtak(): Vedtak = innvilgelse(
        behandlingId = behandlingId,
        sakId = sakId,
        vedtakstidspunkt = vedtakstidspunkt,
        virkningsdato = virkningsdato,
        dagsats = dagsats,
        stønadsdager = stønadsdager,
        hovedrettighet = hovedrettighet,
        vanligArbeidstidPerDag = vanligArbeidstidPerDag,
    )

    override fun kontekstMap(): Map<String, String> = emptyMap()
}

class RettighetBehandletOgAvslåttHendelse(
    meldingsreferanseId: UUID,
    sakId: String,
    ident: String,
    behandlingId: UUID,
    vedtakstidspunkt: LocalDateTime,
    virkningsdato: LocalDate,
    private val hovedrettighet: Hovedrettighet,
) : RettighetBehandletHendelse(
    meldingsreferanseId = meldingsreferanseId,
    sakId = sakId,
    ident = ident,
    behandlingId = behandlingId,
    vedtakstidspunkt = vedtakstidspunkt,
    virkningsdato = virkningsdato,
) {
    override fun tilVedtak(): Vedtak = avslag(
        sakId = sakId,
        behandlingId = behandlingId,
        vedtakstidspunkt = vedtakstidspunkt,
        virkningsdato = virkningsdato,
        dagpengerettighet = hovedrettighet,
    )
    override fun kontekstMap(): Map<String, String> = emptyMap()
}
