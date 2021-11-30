package no.nav.dagpenger.vedtak.modell

import no.nav.dagpenger.vedtak.modell.beregningsregler.Beregningsregel
import no.nav.dagpenger.vedtak.modell.hendelse.BokføringsHendelseType
import no.nav.dagpenger.vedtak.modell.konto.Konto
import java.time.LocalDate

typealias Mengde = Int

internal class Avtale {
    private val beregningsregler = mutableMapOf<BokføringsHendelseType, TemporalCollection<Beregningsregel>>()
    private val kontoer = mutableMapOf<String, Konto>()

    internal fun erAktiv() = true

    fun leggTilKonto(navn: String, konto: Konto) {
        kontoer[navn] = konto
    }

    internal fun endre() {
        TODO("Not yet implemented")
    }

    fun leggTilBeregningsregel(type: BokføringsHendelseType, beregningsregel: Beregningsregel, fraOgMed: LocalDate) {
        beregningsregler.computeIfAbsent(type) {
            TemporalCollection()
        }.put(fraOgMed, beregningsregel)
    }

    fun finnBeregningsregel(type: BokføringsHendelseType, fraOgMed: LocalDate) =
        beregningsregler[type]?.get(fraOgMed) ?: throw IllegalArgumentException("Finnes ingen beregningsregler for denne typen")

    fun balanse(konto: String) = kontoer[konto]?.balanse()
}
