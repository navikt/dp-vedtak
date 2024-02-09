package no.nav.dagpenger.vedtak.modell

import no.nav.dagpenger.aktivitetslogg.Aktivitet
import no.nav.dagpenger.opplysning.Opplysninger
import no.nav.dagpenger.opplysning.Opplysningstype
import no.nav.dagpenger.opplysning.Regelkjøring
import no.nav.dagpenger.opplysning.Regelsett
import no.nav.dagpenger.vedtak.modell.hendelser.SøkerHendelse

class Behandling(
    private val behandler: SøkerHendelse,
    private val opplysninger: Opplysninger,
    private vararg val regelsett: Regelsett,
) {
    private val regelkjøring = Regelkjøring(behandler.gjelderDato, opplysninger, *regelsett)

    fun trenger(opplysningstype: Opplysningstype<Boolean>) = regelkjøring.trenger(opplysningstype)
}

data class OpplysningBehov(override val name: String) : Aktivitet.Behov.Behovtype
