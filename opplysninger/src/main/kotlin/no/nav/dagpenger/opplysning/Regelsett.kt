package no.nav.dagpenger.opplysning

import no.nav.dagpenger.opplysning.regel.Ekstern
import no.nav.dagpenger.opplysning.regel.Regel
import java.net.URI
import java.time.LocalDate

enum class RegelsettType {
    Vilkår,
    Fastsettelse,
}

data class Lovkilde(
    val navn: String,
    val kortnavn: String,
    val url: URI? = null,
) {
    fun hjemmel(
        kapittel: Int,
        paragraf: Int,
        tittel: String,
        kortnavn: String,
    ) = Hjemmel(this, kapittel, paragraf, tittel, kortnavn)

    override fun toString() = kortnavn
}

data class Hjemmel(
    val kilde: Lovkilde,
    val kapittel: Int,
    val paragraf: Int,
    val tittel: String,
    val kortnavn: String,
) {
    val url = kilde.url?.resolve("§$kapittel-$paragraf")

    override fun toString() = "§ $kapittel-$paragraf. $tittel"
}

class Regelsett(
    val hjemmel: Hjemmel,
    val type: RegelsettType,
    block: Regelsett.() -> Unit = {},
) {
    constructor(
        navn: String,
        block: Regelsett.() -> Unit = {},
    ) : this(Hjemmel(Lovkilde(navn, navn), 0, 0, navn, navn), RegelsettType.Vilkår, block)

    constructor(hjemmel: Hjemmel, block: Regelsett.() -> Unit = {}) : this(hjemmel, RegelsettType.Vilkår, block)

    private val regler: MutableMap<Opplysningstype<*>, TemporalCollection<Regel<*>>> = mutableMapOf()
    private val avklaringer: MutableSet<Avklaringkode> = mutableSetOf()
    private var _utfall: Opplysningstype<Boolean>? = null
    private var relevant: (opplysninger: LesbarOpplysninger) -> Boolean = { true }
    val utfall get() = _utfall
    val navn = hjemmel.kortnavn

    init {
        block()
    }

    fun regler(forDato: LocalDate = LocalDate.MIN) = regler.map { it.value.get(forDato) }.toList()

    fun avklaring(avklaringkode: Avklaringkode) = avklaringer.add(avklaringkode)

    fun avklaringer() = avklaringer.toSet()

    fun relevantHvis(block: (opplysninger: LesbarOpplysninger) -> Boolean) {
        relevant = block
    }

    fun erRelevant(opplysninger: LesbarOpplysninger) = relevant(opplysninger)

    fun utfall(
        produserer: Opplysningstype<Boolean>,
        gjelderFraOgMed: LocalDate = LocalDate.MIN,
        block: Opplysningstype<Boolean>.() -> Regel<*>,
    ) = regel(produserer, gjelderFraOgMed, block).also {
        _utfall = produserer
    }

    fun <T : Comparable<T>> regel(
        produserer: Opplysningstype<T>,
        gjelderFraOgMed: LocalDate = LocalDate.MIN,
        block: Opplysningstype<T>.() -> Regel<*>,
    ) = leggTil(gjelderFraOgMed, produserer.block())

    private fun leggTil(
        gjelderFra: LocalDate,
        regel: Regel<*>,
    ) = regler.computeIfAbsent(regel.produserer) { TemporalCollection() }.put(gjelderFra, regel)

    val produserer: Set<Opplysningstype<*>>
        by lazy { regler.map { it.key }.toSet() }

    fun produserer(opplysningstype: Opplysningstype<*>) = produserer.contains(opplysningstype)

    val avhengerAv: Set<Opplysningstype<*>>
        by lazy {
            regler.flatMap { it.value.getAll().flatMap { regel -> regel.avhengerAv } }.toSet().minus(produserer)
        }

    fun avhengerAv(opplysningstype: Opplysningstype<*>) = avhengerAv.contains(opplysningstype)

    val behov by lazy {
        regler
            .flatMap { it.value.getAll() }
            .toSet()
            .filterIsInstance<Ekstern<*>>()
            .map { it.produserer }
    }

    override fun toString() = "Regelsett(navn=${hjemmel.kortnavn})"
}
