package no.nav.dagpenger.opplysning.regel

import no.nav.dagpenger.opplysning.LesbarOpplysninger
import no.nav.dagpenger.opplysning.Opplysningstype
import no.nav.dagpenger.opplysning.verdier.Stønadsperiode

class HøyesteAv(
    produserer: Opplysningstype<Stønadsperiode>,
    vararg val opplysningstyper: Opplysningstype<Stønadsperiode>,
) : Regel<Stønadsperiode>(produserer, opplysningstyper.toList()) {
    override fun kjør(opplysninger: LesbarOpplysninger) =
        opplysningstyper.maxOfOrNull { opplysningstype -> opplysninger.finnOpplysning(opplysningstype).verdi } ?: Stønadsperiode(0)
}

fun Opplysningstype<Stønadsperiode>.høyesteAv(vararg opplysningstype: Opplysningstype<Stønadsperiode>) = HøyesteAv(this, *opplysningstype)
