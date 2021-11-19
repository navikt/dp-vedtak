package no.nav.dagpenger.vedtak.modell

import no.nav.dagpenger.vedtak.modell.beregningsregler.SatsBeregningsregel
import no.nav.dagpenger.vedtak.modell.hendelse.AvslagHendelse
import no.nav.dagpenger.vedtak.modell.hendelse.InnvilgetProsessresultatHendelse
import no.nav.dagpenger.vedtak.modell.hendelse.StansHendelse

class Person private constructor(
    val avtaler: MutableList<Avtale>,
    val vedtak: MutableList<Vedtak>,
) {
    constructor() : this(mutableListOf(), mutableListOf())

    companion object {
        internal fun MutableList<Avtale>.gjeldende() = this.lastOrNull() // TODO sjekk aktiv
    }

    fun håndter(innvilgetProsessresultatHendelse: InnvilgetProsessresultatHendelse) {
        avtaler.gjeldende().also { avtale ->
            if (avtale != null) {
                avtale.endre()
                avtale.leggTilBeregningsregel(SatsBeregningsregel(sats = innvilgetProsessresultatHendelse.sats))
                vedtak.add(InnvilgetEndringsvedtak(innvilgetProsessresultatHendelse, avtale))
            } else {
                Avtale().also {
                    avtaler.add(it)
                    it.leggTilBeregningsregel(SatsBeregningsregel(sats = innvilgetProsessresultatHendelse.sats))
                    vedtak.add(Hovedvedtak(innvilgetProsessresultatHendelse, it))
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

       fun håndter(nyttBarnVurdertHendelse: BarnetilleggSkalInnvilgesHendelse) {
           avtaler.gjeldende().also {
               it.leggTilBeregningsregel(SatsBeregningsregel(sats = nyttBarnVurdertHendelse.sats))
               vedtak.add(Endringsvedtak(nyttBarnVurdertHendelse, it))
           }
       }

       fun håndter(innsendtMeldekortHendelse: InnsendtMeldekortHendelse) {
           avtaler.gjeldende().leggTilBeregningsregel(
               StønadsperiodeBeregningsregel(innsendtMeldekortHendelse.periode)
           )
       }

       fun håndter(barnetilleggSkalAvslåsHendelse: BarnetilleggSkalAvslåsHendelse) =
           vedtak.add(Endringsvedtak(barnetilleggSkalAvslåsHendelse, avtaler.gjeldende()))

}
