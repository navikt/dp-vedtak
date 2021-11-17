package no.nav.dagpenger.vedtak.modell

import no.nav.dagpenger.vedtak.modell.hendelse.AvslagHendelse
import no.nav.dagpenger.vedtak.modell.hendelse.InnvilgetProsessresultatHendelse
import no.nav.dagpenger.vedtak.modell.hendelse.NyttBarnVurdertHendelse
import no.nav.dagpenger.vedtak.modell.hendelse.StansHendelse

class Person private constructor(
    val avtaler: MutableList<Avtale>,
    val vedtak: MutableList<Vedtak>,
) {
    constructor() : this(mutableListOf(), mutableListOf())

    fun håndter(innvilgetProsessresultatHendelse: InnvilgetProsessresultatHendelse) {

        if(avtaler.isNotEmpty()){//egentlig om man har aktiv
            avtaler.last().also { avtale ->
                avtale.endre()
                vedtak.add(Endringsvedtak(innvilgetProsessresultatHendelse, avtale))
            }
        }
        Avtale().also {
            avtaler.add(it)
            vedtak.add(Hovedvedtak(innvilgetProsessresultatHendelse, it))
        }
    }

    fun håndter(avslagHendelse: AvslagHendelse) {
        vedtak.add(Hovedvedtak(avslagHendelse))
    }

    fun håndter(stansHendelse: StansHendelse) {
        avtaler.last().also {
            // her må det skje noe
            vedtak.add(Stansvedtak(stansHendelse, it))
        }
    }

    fun håndter(nyttBarnHendelse: NyttBarnVurdertHendelse) {
        val nyttBarnHendelse =
    }
}
