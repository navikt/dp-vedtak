package no.nav.dagpenger.vedtak.modell.hendelser

import no.nav.dagpenger.vedtak.modell.Dagpengerettighet
import no.nav.dagpenger.vedtak.modell.Vedtak
import no.nav.dagpenger.vedtak.modell.entitet.Timer.Companion.timer
import no.nav.dagpenger.vedtak.modell.mengde.Enhet.Companion.arbeidsuker
import java.time.LocalDate
import java.util.UUID

class SøknadBehandletHendelse(ident: String, private val behandlingId: UUID, private val utfall: Boolean, private val virkningsdato: LocalDate) {

    fun behandlingId() = behandlingId

    fun tilVedtak(): Vedtak {
        return when (utfall) {
            true -> Vedtak.innvilgelse(
                virkningsdato,
                1000.toBigDecimal(),
                20.toBigDecimal(),
                52.arbeidsuker,
                Dagpengerettighet.OrdinæreDagpenger,
                40.timer,
            )
            false -> Vedtak.avslag(virkningsdato)
        }
    }
}
