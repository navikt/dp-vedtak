package no.nav.dagpenger.vedtak.modell

import java.util.UUID

class VedtakIdentifikator(private val ident: UUID) {

    companion object {
        fun String.tilVedtakIdentifikator() = VedtakIdentifikator(UUID.fromString(this))
    }

    fun identifikator() = ident

    override fun equals(other: Any?): Boolean = other is VedtakIdentifikator && other.ident == this.ident
}
