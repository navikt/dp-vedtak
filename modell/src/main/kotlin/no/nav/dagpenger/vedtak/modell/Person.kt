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
import java.util.UUID

class Person private constructor(
    private val avtaler: MutableList<Avtale>,
    private val vedtak: MutableList<Vedtak>,
    private val observers: MutableList<PersonObserver>,
) {
    constructor() : this(
        avtaler = mutableListOf(),
        vedtak = mutableListOf(),
        observers = mutableListOf()
    )

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
            leggTilVedtak(InnvilgetEndringsvedtak(innvilgetProsessresultatHendelse, avtale))

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

                leggTilVedtak(Hovedvedtak(innvilgetProsessresultatHendelse, it))
            }
    }

    private fun leggTilVedtak(nyttVedtak: Vedtak) {
        vedtak.add(nyttVedtak)
        emitVedtak(nyttVedtak)
    }

    fun håndter(avslagHendelse: AvslagHendelse) {
        leggTilVedtak(Hovedvedtak(avslagHendelse))
    }

    fun håndter(stansHendelse: StansHendelse) {
        avtaler.gjeldende().also {
            // her må det skje noe
            leggTilVedtak(Stansvedtak(stansHendelse, it))
        }
    }

    fun håndter(hendelse: ArenaKvoteForbruk) {
        val kvotebruk = Kvotebruk(hendelse.mengde, LocalDate.now(), LocalDate.now(), this)
        kvotebruk.håndter()
    }

    fun håndter(barnetilleggSkalAvslåsHendelse: BarnetilleggSkalAvslåsHendelse) =
        leggTilVedtak(InnvilgetEndringsvedtak(barnetilleggSkalAvslåsHendelse, gjeldendeAvtale()))

    internal fun gjeldendeAvtale() = avtaler.gjeldende()

    fun addObserver(observer: PersonObserver) {
        observers.add(observer)
    }

    private fun emitVedtak(vedtak: Vedtak) {
        PersonObserver.VedtakFattetEvent(
            vedtakId = UUID.randomUUID(),
            avtaleId = vedtak.avtale?.avtaleId,
            sats = vedtak.avtale?.sats()
        ).also { fattet ->
            observers.forEach { it.vedtakFattet(fattet) }
        }
    }
}
