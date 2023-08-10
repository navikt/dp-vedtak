package no.nav.dagpenger.vedtak.modell.hendelser

import no.nav.dagpenger.aktivitetslogg.Aktivitetslogg
import no.nav.dagpenger.vedtak.modell.Dagpengerettighet
import no.nav.dagpenger.vedtak.modell.entitet.Beløp
import no.nav.dagpenger.vedtak.modell.entitet.Stønadsdager
import no.nav.dagpenger.vedtak.modell.entitet.Timer
import no.nav.dagpenger.vedtak.modell.vedtak.Avslag.Companion.avslag
import no.nav.dagpenger.vedtak.modell.vedtak.Rammevedtak.Companion.innvilgelse
import no.nav.dagpenger.vedtak.modell.vedtak.Vedtak
import java.time.LocalDate
import java.time.LocalDateTime
import java.util.UUID

sealed class SøknadBehandletHendelse(
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

class DagpengerInnvilgetHendelse(
    meldingsreferanseId: UUID,
    sakId: String,
    ident: String,
    behandlingId: UUID,
    vedtakstidspunkt: LocalDateTime,
    virkningsdato: LocalDate,
    private val dagpengerettighet: Dagpengerettighet,
    private val dagsats: Beløp,
    private val stønadsdager: Stønadsdager,
    private val vanligArbeidstidPerDag: Timer,
    private val egenandel: Beløp,
) : SøknadBehandletHendelse(
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
        dagpengerettighet = dagpengerettighet,
        vanligArbeidstidPerDag = vanligArbeidstidPerDag,
        egenandel = egenandel,
    )

    override fun kontekstMap(): Map<String, String> = emptyMap()
}

class DagpengerAvslåttHendelse(
    meldingsreferanseId: UUID,
    sakId: String,
    ident: String,
    behandlingId: UUID,
    vedtakstidspunkt: LocalDateTime,
    virkningsdato: LocalDate,
    private val dagpengerettighet: Dagpengerettighet,
) : SøknadBehandletHendelse(
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
        dagpengerettighet = dagpengerettighet,
    )
    override fun kontekstMap(): Map<String, String> = emptyMap()
}
