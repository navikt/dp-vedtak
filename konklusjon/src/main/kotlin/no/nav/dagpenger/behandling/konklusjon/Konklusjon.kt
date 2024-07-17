package no.nav.dagpenger.behandling.konklusjon

import no.nav.dagpenger.opplysning.LesbarOpplysninger
import no.nav.dagpenger.opplysning.Opplysninger
import no.nav.dagpenger.opplysning.Opplysningstype
import no.nav.dagpenger.opplysning.Regelkjøring
import no.nav.dagpenger.opplysning.Regelsett
import java.time.LocalDate

class Konklusjon(
    opplysninger: LesbarOpplysninger,
    vararg kontroller: Regelsett,
) {
    constructor(opplysninger: LesbarOpplysninger, kontroller: List<Regelsett>) : this(opplysninger, *kontroller.toTypedArray())

    private val forDato = LocalDate.now()

    private val kontrollopplysninger = Opplysninger(opplysninger as Opplysninger)
    private val regelkjøring = Regelkjøring(forDato, kontrollopplysninger, *kontroller)

    fun kanKonkludere(vararg utfall: Opplysningstype<Boolean>): Boolean {
        regelkjøring.evaluer()

        return utfall.all {
            kontrollopplysninger.har(it) && kontrollopplysninger.finnOpplysning(it).verdi
        }
    }
}
