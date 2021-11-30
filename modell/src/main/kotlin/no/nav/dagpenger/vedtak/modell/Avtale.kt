package no.nav.dagpenger.vedtak.modell

import no.nav.dagpenger.vedtak.modell.beregningsregler.Beregningsregel
import no.nav.dagpenger.vedtak.modell.beregningsregler.SatsBeregningsregel
import no.nav.dagpenger.vedtak.modell.beregningsregler.StønadsperiodeBeregningsregel
import no.nav.dagpenger.vedtak.modell.hendelse.BokføringsHendelseType
import no.nav.dagpenger.vedtak.modell.konto.Konto
import java.time.LocalDate

/*private fun ordinær(sats) = Avtale().also {}
private val perm = ordinær.also { it.leggTilBeregningsregel() }
private val fisk = Avtale().also {}
private val konkurs = Avtale().also {}
private val utdanning = Avtale().also {}
private val verneplikt = Avtale().also {}*/

internal class Avtale {
    private val beregningsregler = mutableMapOf<BokføringsHendelseType, TemporalCollection<Beregningsregel>>()
    private val kontoer = mutableMapOf<String, Konto>()

    companion object {
        fun ordinær(sats: Double, fraOgMed: LocalDate = LocalDate.now()) = Avtale().also { avtale ->
            avtale.leggTilKonto(
                "Stønadsperiodekonto",
                Konto().also {
                    avtale.leggTilBeregningsregel(
                        BokføringsHendelseType.Kvotebruk,
                        StønadsperiodeBeregningsregel(it),
                        fraOgMed
                    )
                }
            )
            avtale.leggTilBeregningsregel(
                BokføringsHendelseType.Meldekort,
                SatsBeregningsregel(sats, Konto()),
                fraOgMed
            )
        }
    }

    internal fun erAktiv() = true

    fun leggTilKonto(navn: String, konto: Konto) {
        kontoer[navn] = konto
    }

    fun leggTilBeregningsregel(type: BokføringsHendelseType, beregningsregel: Beregningsregel, fraOgMed: LocalDate) {
        beregningsregler.computeIfAbsent(type) {
            TemporalCollection()
        }.put(fraOgMed, beregningsregel)
    }

    fun finnBeregningsregel(type: BokføringsHendelseType, fraOgMed: LocalDate) =
        beregningsregler[type]?.get(fraOgMed)
            ?: throw IllegalArgumentException("Finnes ingen beregningsregler for denne typen")

    fun balanse(konto: String) = kontoer[konto]?.balanse()
}
