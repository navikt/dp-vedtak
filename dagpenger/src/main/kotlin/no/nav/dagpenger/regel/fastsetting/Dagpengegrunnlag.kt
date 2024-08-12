package no.nav.dagpenger.regel.fastsetting

import no.nav.dagpenger.opplysning.Opplysningstype
import no.nav.dagpenger.regel.Verneplikt

object Dagpengegrunnlag {
    val inntekt = Opplysningstype.somInntekt("Inntekt")
    val oppjustertinntekt = Opplysningstype.somInntekt("Oppjustert inntekt")
    val verneplikt = Verneplikt.vurderingAvVerneplikt
    val avkortet = Opplysningstype.somBeløp("Avkortet grunnlag")
    val uavkortet = Opplysningstype.somBeløp("Uavkortet grunnlag")
}
