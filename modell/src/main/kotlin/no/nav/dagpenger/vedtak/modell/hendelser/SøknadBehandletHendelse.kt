package no.nav.dagpenger.vedtak.modell.hendelser

import no.nav.dagpenger.vedtak.modell.Aktivitetslogg
import no.nav.dagpenger.vedtak.modell.Dagpengerettighet
import no.nav.dagpenger.vedtak.modell.entitet.Timer
import no.nav.dagpenger.vedtak.modell.mengde.Stønadsperiode
import no.nav.dagpenger.vedtak.modell.vedtak.Vedtak
import java.math.BigDecimal
import java.time.LocalDate
import java.util.UUID

sealed class SøknadBehandletHendelse(
    protected val ident: String,
    internal val behandlingId: UUID,
    protected val virkningsdato: LocalDate,
    aktivitetslogg: Aktivitetslogg = Aktivitetslogg(),
) : Hendelse(ident, aktivitetslogg) {
    abstract fun tilVedtak(): Vedtak
}

class DagpengerInnvilgetHendelse(
    ident: String,
    behandlingId: UUID,
    virkningsdato: LocalDate,
    private val dagpengerettighet: Dagpengerettighet,
    private val dagsats: BigDecimal,
    private val grunnlag: BigDecimal,
    private val stønadsperiode: Stønadsperiode,
    private val vanligArbeidstidPerDag: Timer,
    private val egenandel: BigDecimal,
) : SøknadBehandletHendelse(
    ident,
    behandlingId,
    virkningsdato,
) {
    override fun tilVedtak(): Vedtak = Vedtak.innvilgelse(
        behandlingId = behandlingId,
        virkningsdato = virkningsdato,
        grunnlag = grunnlag,
        dagsats = dagsats,
        stønadsperiode = stønadsperiode,
        dagpengerettighet = dagpengerettighet,
        vanligArbeidstidPerDag = vanligArbeidstidPerDag,
        egenandel = egenandel,
    )

    override fun kontekstMap(): Map<String, String> = emptyMap()
}

class DagpengerAvslåttHendelse(ident: String, behandlingId: UUID, virkningsdato: LocalDate) :
    SøknadBehandletHendelse(
        ident,
        behandlingId,
        virkningsdato,
    ) {
    override fun tilVedtak(): Vedtak = Vedtak.avslag(behandlingId, virkningsdato)
    override fun kontekstMap(): Map<String, String> = emptyMap()
}
