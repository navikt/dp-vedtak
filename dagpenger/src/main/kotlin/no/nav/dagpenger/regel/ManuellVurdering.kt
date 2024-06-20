package no.nav.dagpenger.regel

import no.nav.dagpenger.opplysning.Opplysningstype
import no.nav.dagpenger.opplysning.Regelsett
import no.nav.dagpenger.opplysning.id
import no.nav.dagpenger.opplysning.regel.innhentMed

object ManuellVurdering {
    private val jobbetUtenForNorge = Opplysningstype.somBoolsk("Arbeid utenfor Norge".id("JobbetUtenforNorge"))
    private val lukkedeSaker = Opplysningstype.somBoolsk("Hatt lukkede saker siste 8 uker".id("HarHattLukketSiste8Uker"))
    private val muligGjenopptak = Opplysningstype.somBoolsk("Mulig gjenopptak".id("HarHattDagpengerSiste13Mnd"))

    val regelsett =
        Regelsett("Manuellvurderinger") {
            regel(jobbetUtenForNorge) { innhentMed() }
            regel(lukkedeSaker) { innhentMed() }
            regel(muligGjenopptak) { innhentMed() }
        }
}
