package no.nav.dagpenger.vedtak.modell.hendelser

import no.nav.dagpenger.vedtak.modell.Dagpengerettighet
import no.nav.dagpenger.vedtak.modell.Vedtak
import no.nav.dagpenger.vedtak.modell.entitet.Timer.Companion.timer
import no.nav.dagpenger.vedtak.modell.mengde.Enhet.Companion.arbeidsuker
import java.time.LocalDate
import java.util.UUID
sealed class SøknadBehandletHendelse(
    ident: String,
    private val behandlingId: UUID,
    protected val virkningsdato: LocalDate,
) {

    fun behandlingId() = behandlingId

    abstract fun tilVedtak(): Vedtak
}

class SøknadInnvilgetHendelse(ident: String, behandlingId: UUID, virkningsdato: LocalDate) :
    SøknadBehandletHendelse(
        ident,
        behandlingId,
        virkningsdato,
    ) {
    override fun tilVedtak(): Vedtak = Vedtak.innvilgelse(
        virkningsdato,
        1000.toBigDecimal(),
        20.toBigDecimal(),
        52.arbeidsuker,
        Dagpengerettighet.OrdinæreDagpenger,
        40.timer,
    )
}

class SøknadAvslåttHendelse(ident: String, behandlingId: UUID, virkningsdato: LocalDate) :
    SøknadBehandletHendelse(
        ident,
        behandlingId,
        virkningsdato,
    ) {
    override fun tilVedtak(): Vedtak = Vedtak.avslag(virkningsdato)
}
