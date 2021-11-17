package no.nav.dagpenger.vedtak.modell

import no.nav.dagpenger.vedtak.modell.beregningsregler.SatsBeregningsregel
import no.nav.dagpenger.vedtak.modell.beregningsregler.StønadsperiodeBeregningsregel
import no.nav.dagpenger.vedtak.modell.hendelse.AvslagHendelse
import no.nav.dagpenger.vedtak.modell.hendelse.InnsendtMeldekortHendelse
import no.nav.dagpenger.vedtak.modell.hendelse.InnvilgetProsessresultatHendelse
import no.nav.dagpenger.vedtak.modell.hendelse.NyttBarnVurdertHendelse
import no.nav.dagpenger.vedtak.modell.hendelse.StansHendelse

class Person private constructor(
    val avtaler: MutableList<Avtale>,
    val vedtak: MutableList<Vedtak>,
) {
    constructor() : this(mutableListOf(), mutableListOf())

    fun håndter(innvilgetProsessresultatHendelse: InnvilgetProsessresultatHendelse) {

        if (avtaler.isNotEmpty()) { // egentlig om man har aktiv
            avtaler.gjeldende().also { avtale ->
                avtale.endre()
                avtale.leggTilBeregningsregel(SatsBeregningsregel(sats = innvilgetProsessresultatHendelse.sats))
                vedtak.add(Endringsvedtak(innvilgetProsessresultatHendelse, avtale))
            }
        } else
            Avtale().also {
                avtaler.add(it)
                it.leggTilBeregningsregel(SatsBeregningsregel(sats = innvilgetProsessresultatHendelse.sats))
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

    fun håndter(nyttBarnVurdertHendelse: NyttBarnVurdertHendelse) {
        if (nyttBarnVurdertHendelse.resultat) {
            avtaler.gjeldende().also {
                requireNotNull(nyttBarnVurdertHendelse.sats) { "Sats kan ikke være null" }
                it.leggTilBeregningsregel(SatsBeregningsregel(sats = nyttBarnVurdertHendelse.sats))
                vedtak.add(Endringsvedtak(nyttBarnVurdertHendelse, it))
            }
        } else {
            vedtak.add(Endringsvedtak(nyttBarnVurdertHendelse, avtaler.gjeldende()))
        }
    }

    fun håndter(innsendtMeldekortHendelse: InnsendtMeldekortHendelse) {
        avtaler.gjeldende().also {
            it.leggTilBeregningsregel(StønadsperiodeBeregningsregel(innsendtMeldekortHendelse.periode))
        }
    }
}

private fun MutableList<Avtale>.gjeldende() = this.last() // TODO sjekk aktiv
