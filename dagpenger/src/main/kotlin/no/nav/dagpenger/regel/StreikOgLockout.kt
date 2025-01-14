package no.nav.dagpenger.regel

import no.nav.dagpenger.opplysning.Opplysningstype
import no.nav.dagpenger.opplysning.Regelsett
import no.nav.dagpenger.opplysning.regel.ingenAv
import no.nav.dagpenger.opplysning.regel.oppslag
import no.nav.dagpenger.regel.Søknadstidspunkt.prøvingsdato

object StreikOgLockout {
    val deltarIStreikOgLockout = Opplysningstype.somBoolsk("Deltar medlemmet i streik eller er omfattet av lock-out?")
    val sammeBedriftOgPåvirket =
        Opplysningstype.somBoolsk(
            "Ledig ved samme bedrift eller arbeidsplass, og blir påvirket av utfallet?",
        )

    val ikkeStreikEllerLockout = Opplysningstype.somBoolsk("Er medlemmet ikke påvirket av streik eller lock-out?")

    val regelsett =
        Regelsett(
            "4-22 Bortfall ved streik og lock-out",
            "§ 4-22. Bortfall ved streik og lock-out",
        ).apply {
            regel(deltarIStreikOgLockout) { oppslag(prøvingsdato) { false } }
            regel(sammeBedriftOgPåvirket) { oppslag(prøvingsdato) { false } }
            utfall(ikkeStreikEllerLockout) { ingenAv(deltarIStreikOgLockout, sammeBedriftOgPåvirket) }
        }
}
