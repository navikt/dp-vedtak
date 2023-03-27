package no.nav.dagpenger.vedtak.modell.rapportering

import no.nav.dagpenger.vedtak.modell.Dagpengerettighet
import no.nav.dagpenger.vedtak.modell.TemporalCollection
import no.nav.dagpenger.vedtak.modell.entitet.Timer
import java.math.BigDecimal
import java.util.UUID

internal class LøpendeBehandling(
    private val rapporteringsId: UUID,
    internal val satshistorikk: TemporalCollection<BigDecimal>,
    internal val rettighethistorikk: TemporalCollection<Dagpengerettighet>,
    internal val vanligarbeidstidhistorikk: TemporalCollection<Timer>,

) {
    private val periode = Beregningsgrunnlag()
    fun håndter(rapporteringsperiode: Rapporteringsperiode) {
        periode.populer(rapporteringsperiode, this)

//
        val ok = TaptArbeidstid().håndter(periode)
//
        if (ok) {
            println("hurra")
        }
    }
}
