package no.nav.dagpenger.regel

import no.nav.dagpenger.opplysning.Hypotese
import no.nav.dagpenger.opplysning.Opplysningstype
import no.nav.dagpenger.opplysning.Regelsett
import no.nav.dagpenger.opplysning.id
import no.nav.dagpenger.opplysning.regel.alle
import no.nav.dagpenger.opplysning.regel.enAv
import no.nav.dagpenger.regel.Behov.HelseTilAlleTyperJobb
import no.nav.dagpenger.regel.Behov.KanJobbeDeltid
import no.nav.dagpenger.regel.Behov.KanJobbeHvorSomHelst
import no.nav.dagpenger.regel.Behov.VilligTilÅBytteYrke

object ReellArbeidssøker {
    internal val kanJobbeDeltid = Opplysningstype.somBoolsk("Kan jobbe heltid og deltid".id(KanJobbeDeltid))
    internal val kanJobbeHvorSomHelst = Opplysningstype.somBoolsk("Kan jobbe i hele Norge".id(KanJobbeHvorSomHelst))
    internal val unntakMobilitet = Opplysningstype.somBoolsk("Bruker oppfyller kravet om unntak til mobilitet")
    private val oppfyllerKravTilMobilitet = Opplysningstype.somBoolsk("Bruker oppfyller kravet til mobilitet")
    internal val helseTilAlleTyperJobb = Opplysningstype.somBoolsk("Kan ta alle typer arbeid".id(HelseTilAlleTyperJobb))
    internal val villigTilÅBytteYrke = Opplysningstype.somBoolsk("Villig til å bytte yrke".id(VilligTilÅBytteYrke))

    val kravTilArbeidssøker = Opplysningstype.somBoolsk("Krav til arbeidssøker")

    val regelsett =
        Regelsett("Reell arbeidssøker") {
            startverdi { Hypotese(unntakMobilitet, false) }

            regel(oppfyllerKravTilMobilitet) { enAv(kanJobbeHvorSomHelst, unntakMobilitet) }
            regel(kravTilArbeidssøker) {
                alle(
                    kanJobbeDeltid,
                    oppfyllerKravTilMobilitet,
                    helseTilAlleTyperJobb,
                    villigTilÅBytteYrke,
                )
            }
        }
}
