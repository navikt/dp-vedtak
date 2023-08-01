package no.nav.dagpenger.aktivitetslogg

abstract class Varselkode {
    abstract val varseltekst: String
    internal fun varsel(kontekster: List<SpesifikkKontekst>): Aktivitet.Varsel =
        Aktivitet.Varsel.opprett(kontekster, this, varseltekst)
    override fun toString() = "${this::class.java.simpleName}: $varseltekst"
}
