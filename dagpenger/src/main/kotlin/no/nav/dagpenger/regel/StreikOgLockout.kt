package no.nav.dagpenger.regel

import no.nav.dagpenger.opplysning.Opplysningstype
import no.nav.dagpenger.opplysning.Regelsett
import no.nav.dagpenger.opplysning.regel.ingenAv
import no.nav.dagpenger.opplysning.regel.oppslag
import no.nav.dagpenger.regel.SøknadInnsendtHendelse.Companion.prøvingsdato

object StreikOgLockout {
    val deltarIStreikOgLockout = Opplysningstype.somBoolsk("Deltar medlemmet i streik eller er omfattet av lock-out?")
    val sammeBedriftOgPåvirket =
        Opplysningstype.somBoolsk(
            "Ledig ved samme bedrift eller arbeidsplass, og blir påvirket av utfallet?",
        )

    val ikkeStreikEllerLockout = Opplysningstype.somBoolsk("Er medlemmet påvirket av streik eller lock-out?")

    val regelsett =
        Regelsett("StreikOgLockout").apply {
            regel(deltarIStreikOgLockout) { oppslag(prøvingsdato) { false } }
            regel(sammeBedriftOgPåvirket) { oppslag(prøvingsdato) { false } }
            regel(ikkeStreikEllerLockout) {
                // TODO: denne blir feil vei, false = true og true = false
                ingenAv(deltarIStreikOgLockout, sammeBedriftOgPåvirket)
            }
        }
}
