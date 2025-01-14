package no.nav.dagpenger.regel

import no.nav.dagpenger.avklaring.Kontrollpunkt
import no.nav.dagpenger.opplysning.Opplysningstype
import no.nav.dagpenger.opplysning.Regelsett
import no.nav.dagpenger.opplysning.regel.oppslag
import no.nav.dagpenger.regel.SamordingUtenforFolketrygden.skalSamordnesUtenforFolketrygden
import no.nav.dagpenger.regel.Samordning.skalSamordnes
import no.nav.dagpenger.regel.Søknadstidspunkt.prøvingsdato

object FulleYtelser {
    val ikkeFulleYtelser = Opplysningstype.somBoolsk("Mottar ikke andre fulle ytelser")

    val regelsett =
        Regelsett("§ 4-24. Medlem som har fulle ytelser etter folketrygdloven eller avtalefestet pensjon") {
            utfall(ikkeFulleYtelser) { oppslag(prøvingsdato) { true } }
        }

    val ønsketResultat = listOf(ikkeFulleYtelser)

    val FulleYtelserKontrollpunkt =
        Kontrollpunkt(sjekker = Avklaringspunkter.FulleYtelser) { opplysninger ->
            listOf(skalSamordnes, skalSamordnesUtenforFolketrygden).any {
                opplysninger.har(it) && opplysninger.finnOpplysning(it).verdi
            }
        }
}
