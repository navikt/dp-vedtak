package no.nav.dagpenger.vedtak.modell

class PersonIdentifikator(private val ident: String) {
    init {
        require(ident.matches(Regex("\\d{11}"))) { "personident må ha 11 siffer" }
    }

    companion object {
        fun String.tilPersonIdentfikator() = PersonIdentifikator(this)
    }

    fun identifikator() = ident

    override fun equals(other: Any?): Boolean = other is PersonIdentifikator && other.ident == this.ident

    override fun hashCode() = ident.hashCode()
}
