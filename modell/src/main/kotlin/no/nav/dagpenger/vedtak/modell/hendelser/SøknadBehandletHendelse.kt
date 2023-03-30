package no.nav.dagpenger.vedtak.modell.hendelser

import no.nav.dagpenger.vedtak.modell.Dagpengerettighet
import no.nav.dagpenger.vedtak.modell.Vedtak
import no.nav.dagpenger.vedtak.modell.entitet.Timer
import no.nav.dagpenger.vedtak.modell.mengde.Stønadsperiode
import java.math.BigDecimal
import java.time.LocalDate
import java.util.UUID

sealed class SøknadBehandletHendelse(
    protected val ident: String,
    protected val behandlingId: UUID,
    protected val virkningsdato: LocalDate,
) {
    abstract fun tilVedtak(): Vedtak
}

class SøknadInnvilgetHendelse(
    ident: String,
    behandlingId: UUID,
    virkningsdato: LocalDate,
    private val dagpengerettighet: Dagpengerettighet,
    private val dagsats: BigDecimal,
    private val grunnlag: BigDecimal,
    private val stønadsperiode: Stønadsperiode,
    private val vanligArbeidstidPerDag: Timer,
    private val antallVentedager: Double,
) :
    SøknadBehandletHendelse(
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
        antallVenteDager = antallVentedager,
    )
}

class SøknadAvslåttHendelse(ident: String, behandlingId: UUID, virkningsdato: LocalDate) :
    SøknadBehandletHendelse(
        ident,
        behandlingId,
        virkningsdato,
    ) {
    override fun tilVedtak(): Vedtak = Vedtak.avslag(behandlingId, virkningsdato)
}
