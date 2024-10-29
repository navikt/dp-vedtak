package no.nav.dagpenger.regel.fastsetting

import no.nav.dagpenger.opplysning.Opplysningstype
import no.nav.dagpenger.opplysning.Regelsett
import no.nav.dagpenger.opplysning.regel.multiplikasjon
import no.nav.dagpenger.opplysning.regel.oppslag
import no.nav.dagpenger.regel.Søknadstidspunkt.prøvingsdato

object VernepliktFastsetting {
    private val faktor = Opplysningstype.somHeltall("Faktor")
    internal val vernepliktGrunnlag = Opplysningstype.somBeløp("Grunnlag for verneplikt")
    internal val vernepliktPeriode = Opplysningstype.somHeltall("Vernepliktperiode")
    private val grunnbeløp = Dagpengegrunnlag.grunnbeløp

    val regelsett =
        Regelsett("VernepliktFastsetting") {
            regel(faktor) { oppslag(prøvingsdato) { 3 } }
            regel(vernepliktGrunnlag) { multiplikasjon(grunnbeløp, faktor) }
            regel(vernepliktPeriode) { oppslag(prøvingsdato) { 26 } }
        }
}
