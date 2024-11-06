package no.nav.dagpenger.opplysning.verdier

import java.time.LocalDate

class Barn(
    val ident: String?,
    val fødselsdato: LocalDate,
    val fornavn: String?,
    val mellomnavn: String?,
    val etternavn: String?,
    val land: String?,
    val kvalifiserer: Boolean,
) : Comparable<Barn> {
    override fun compareTo(other: Barn): Int =
        if (this.ident != null && other.ident != null) {
            this.ident.compareTo(other.ident)
        } else {
            0
        }

    override fun equals(other: Any?): Boolean =
        other is Barn &&
            this.ident == other.ident &&
            this.fødselsdato == other.fødselsdato &&
            this.fornavn == other.fornavn &&
            this.mellomnavn == other.mellomnavn &&
            this.etternavn == other.etternavn &&
            this.land == other.land &&
            this.kvalifiserer == other.kvalifiserer

    override fun hashCode(): Int {
        var result = ident?.hashCode() ?: 0
        result = 31 * result + fødselsdato.hashCode()
        result = 31 * result + (fornavn?.hashCode() ?: 0)
        result = 31 * result + (mellomnavn?.hashCode() ?: 0)
        result = 31 * result + (etternavn?.hashCode() ?: 0)
        result = 31 * result + (land?.hashCode() ?: 0)
        result = 31 * result + kvalifiserer.hashCode()
        return result
    }
}
