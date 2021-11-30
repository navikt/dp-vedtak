package no.nav.dagpenger.vedtak.modell

import no.nav.dagpenger.vedtak.modell.beregningsregler.SatsBeregningsregel
import no.nav.dagpenger.vedtak.modell.beregningsregler.StønadsperiodeBeregningsregel
import no.nav.dagpenger.vedtak.modell.hendelse.AvslagHendelse
import no.nav.dagpenger.vedtak.modell.hendelse.BarnetilleggSkalAvslåsHendelse
import no.nav.dagpenger.vedtak.modell.hendelse.InnvilgetProsessresultatHendelse
import no.nav.dagpenger.vedtak.modell.hendelse.Kvotebruk
import no.nav.dagpenger.vedtak.modell.hendelse.StansHendelse
import no.nav.dagpenger.vedtak.modell.konto.Konto
import java.time.LocalDate

class Person private constructor(
    val avtaler: MutableList<Avtale>,
    val vedtak: MutableList<Vedtak>,
) {
    constructor() : this(mutableListOf(), mutableListOf())

    companion object {
        internal fun MutableList<Avtale>.gjeldende() = this.firstOrNull { avtale -> avtale.erAktiv() }
    }

    fun håndter(innvilgetProsessresultatHendelse: InnvilgetProsessresultatHendelse) {

        avtaler.gjeldende().also { avtale ->
            if (avtale != null) {
                avtale.endre()
                avtale.leggTilBeregningsregel(SatsBeregningsregel(sats = innvilgetProsessresultatHendelse.sats, Konto()))
                vedtak.add(InnvilgetEndringsvedtak(innvilgetProsessresultatHendelse, avtale))
            } else {
                Avtale().also { avtale ->
                    avtaler.add(avtale)
                    avtale.leggTilKonto(
                        "Stønadsperiodekonto",
                        Konto().also {
                            avtale.leggTilBeregningsregel(StønadsperiodeBeregningsregel(it))
                            Kvotebruk(innvilgetProsessresultatHendelse.periode, LocalDate.now(), LocalDate.now(), this).håndter()
                        }
                    )
                    avtale.leggTilBeregningsregel(SatsBeregningsregel(sats = innvilgetProsessresultatHendelse.sats, Konto()))
                    vedtak.add(Hovedvedtak(innvilgetProsessresultatHendelse, avtale))
                }
            }
        }
    }

    fun håndter(avslagHendelse: AvslagHendelse) {
        vedtak.add(Hovedvedtak(avslagHendelse))
    }

    fun håndter(stansHendelse: StansHendelse) {
        avtaler.gjeldende().also {
            // her må det skje noe
            vedtak.add(Stansvedtak(stansHendelse, it))
        }
    }

    fun håndter(barnetilleggSkalAvslåsHendelse: BarnetilleggSkalAvslåsHendelse) =
        vedtak.add(InnvilgetEndringsvedtak(barnetilleggSkalAvslåsHendelse, avtaler.gjeldende()))

    internal fun gjeldendeAvtale() = avtaler.last()
}
