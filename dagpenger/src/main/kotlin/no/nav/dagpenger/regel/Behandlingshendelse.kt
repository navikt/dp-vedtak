package no.nav.dagpenger.regel

import no.nav.dagpenger.opplysning.Opplysninger
import no.nav.dagpenger.opplysning.Regelkjøring
import java.time.LocalDate

interface Behandlingshendelse {
    val regelverksdato: LocalDate
    val prøvingsdato: LocalDate

    fun regelkjøring(opplysninger: Opplysninger): Regelkjøring
}

/*class Søknadhendelse : Behandlingshendelse {
    fun behandle(behandling: Behandling) {
        // Innhent opplysninger til vi kan konkludere rett til dagpenger (boolean)
    }
}

class Meldekorthendelse(
    override val regelverksdato: LocalDate,
    private val meldeperiodeFraOgMed: LocalDate,
    private val meldeperiodeTilOgMed: LocalDate,
) : Behandlingshendelse {
    fun opprett(): Opplysninger = Opplysninger()

    fun behandle(behandling: Behandling) {
        // Opprett beregning
        val beregning =
            BeregningsperiodeFabrikk(
                meldeperiodeFraOgMed,
                meldeperiodeTilOgMed,
                behandling.opplysninger(),
            ).lagBeregningsperiode()

        // Skriv resultat av beregning tilbake til opplsyningene i behandlingen
        behandling.opplysninger().leggTil(beregning)
    }

    override fun regelkjøring(opplysninger: Opplysninger): Regelkjøring = Regelkjøring(regelverksdato, opplysninger)
}
*/
