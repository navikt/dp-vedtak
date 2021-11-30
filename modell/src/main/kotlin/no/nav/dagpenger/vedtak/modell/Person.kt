package no.nav.dagpenger.vedtak.modell

import no.nav.dagpenger.vedtak.modell.beregningsregler.SatsBeregningsregel
import no.nav.dagpenger.vedtak.modell.hendelse.ArenaKvoteForbruk
import no.nav.dagpenger.vedtak.modell.hendelse.AvslagHendelse
import no.nav.dagpenger.vedtak.modell.hendelse.BarnetilleggSkalAvslåsHendelse
import no.nav.dagpenger.vedtak.modell.hendelse.BokføringsHendelseType
import no.nav.dagpenger.vedtak.modell.hendelse.InnvilgetProsessresultatHendelse
import no.nav.dagpenger.vedtak.modell.hendelse.Kvotebruk
import no.nav.dagpenger.vedtak.modell.hendelse.StansHendelse
import no.nav.dagpenger.vedtak.modell.konto.Konto
import java.time.LocalDate

class Person private constructor(
    internal val avtaler: MutableList<Avtale>,
    internal val vedtak: MutableList<Vedtak>,
) {
    constructor() : this(mutableListOf(), mutableListOf())

    companion object {
        private fun MutableList<Avtale>.gjeldende() = this.firstOrNull { avtale -> avtale.erAktiv() }
    }

    fun håndter(innvilgetProsessresultatHendelse: InnvilgetProsessresultatHendelse) {
        gjeldendeAvtale()?.let { avtale ->
            avtale.leggTilBeregningsregel(
                BokføringsHendelseType.Meldekort,
                SatsBeregningsregel(sats = innvilgetProsessresultatHendelse.sats, Konto()),
                LocalDate.now()
            )
            vedtak.add(InnvilgetEndringsvedtak(innvilgetProsessresultatHendelse, avtale))

            return
        }

        Avtale.ordinær(innvilgetProsessresultatHendelse.sats)
            .also {
                avtaler.add(it)

                Kvotebruk(
                    innvilgetProsessresultatHendelse.periode,
                    LocalDate.now(),
                    LocalDate.now(),
                    this
                ).håndter()

                vedtak.add(Hovedvedtak(innvilgetProsessresultatHendelse, it))
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

    fun håndter(hendelse: ArenaKvoteForbruk) {
        Kvotebruk(hendelse.mengde, LocalDate.now(), LocalDate.now(), this).håndter()
    }

    fun håndter(barnetilleggSkalAvslåsHendelse: BarnetilleggSkalAvslåsHendelse) =
        vedtak.add(InnvilgetEndringsvedtak(barnetilleggSkalAvslåsHendelse, avtaler.gjeldende()))

    internal fun gjeldendeAvtale() = avtaler.gjeldende()
}
