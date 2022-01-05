package no.nav.dagpenger.vedtak.modell

import no.nav.dagpenger.vedtak.modell.beregningsregler.Beregningsregel
import no.nav.dagpenger.vedtak.modell.beregningsregler.SatsBeregningsregel
import no.nav.dagpenger.vedtak.modell.hendelse.BokføringsHendelseType
import no.nav.dagpenger.vedtak.modell.konto.Konto
import no.nav.dagpenger.vedtak.modell.mengder.Penger
import java.time.LocalDate
import java.util.UUID

internal class Avtale private constructor(
    internal val avtaleId: UUID,
    private val beregningsregler: MutableMap<BokføringsHendelseType, TemporalCollection<Beregningsregel>>,
    private val kontoer: MutableMap<String, Konto>
) {
    private constructor(avtaleId: UUID) : this(
        avtaleId,
        mutableMapOf(),
        mutableMapOf()
    )

    constructor() : this(
        avtaleId = UUID.randomUUID(),
        beregningsregler = mutableMapOf<BokføringsHendelseType, TemporalCollection<Beregningsregel>>(),
        kontoer = mutableMapOf<String, Konto>()
    )

    companion object {
        fun ordinær(sats: Double, fraOgMed: LocalDate = LocalDate.now()) = Avtale().also { avtale ->
            avtale.leggTilKonto(
                "Stønadsperiodekonto",
                Konto.forStønadsperiode(avtale, fraOgMed)
            )
            avtale.leggTilBeregningsregel(
                BokføringsHendelseType.Meldekort,
                SatsBeregningsregel(sats, Konto()),
                fraOgMed
            )
        }

        val avslag: Avtale = Avtale(avtaleId = UUID.fromString("00000000-0000-0000-0000-000000000000"))
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

    fun sats(): Penger {
        if (this.avtaleId == avslag.avtaleId) return Penger(-1)

        val beregningsregel = finnBeregningsregel(BokføringsHendelseType.Meldekort, LocalDate.now())
        require(beregningsregel is SatsBeregningsregel)
        return Penger(beregningsregel.sats)
    }
}
