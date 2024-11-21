package no.nav.dagpenger.regel

import no.nav.dagpenger.avklaring.Kontrollpunkt
import no.nav.dagpenger.opplysning.Opplysningstype
import no.nav.dagpenger.opplysning.Regelsett
import no.nav.dagpenger.opplysning.id
import no.nav.dagpenger.opplysning.regel.enAv
import no.nav.dagpenger.opplysning.regel.innhentes
import no.nav.dagpenger.opplysning.regel.multiplikasjon
import no.nav.dagpenger.opplysning.regel.oppslag
import no.nav.dagpenger.regel.Behov.AndreØkonomiskeYtelser
import no.nav.dagpenger.regel.Behov.OppgittAndreYtelserUtenforNav
import no.nav.dagpenger.regel.Minsteinntekt.grunnbeløp
import no.nav.dagpenger.regel.SamordingUtenforFolketrygden.andreYtelser
import no.nav.dagpenger.regel.SamordingUtenforFolketrygden.skalSamordnesUtenforFolketrygden
import no.nav.dagpenger.regel.Søknadstidspunkt.prøvingsdato

object SamordingUtenforFolketrygden {
    val andreYtelser = Opplysningstype.somBoolsk("Oppgitt andre ytelser utenfor NAV i søknaden".id(OppgittAndreYtelserUtenforNav))

    val pensjonFraOffentligTjenestepensjonsordning = Opplysningstype.somBoolsk("Mottar pensjon fra en offentlig tjenestepensjonsordning")
    val redusertUførepensjon = Opplysningstype.somBoolsk("Mottar redusert uførepensjon fra offentlig pensjonsordning")
    val vartpenger = Opplysningstype.somBoolsk("Mottar vartpenger")
    val ventelønn = Opplysningstype.somBoolsk("Mottar ventelønn")
    val etterlønn = Opplysningstype.somBoolsk("Mottar etterlønn")
    val garantilottGFF = Opplysningstype.somBoolsk("Mottar garantilott fra Garantikassen for fiskere.")
    val andreØkonomiskeYtelser =
        Opplysningstype.somBoolsk(
            "Mottar andre økonomiske ytelser fra arbeidsgiver eller tidligere arbeidsgiver enn lønn".id(AndreØkonomiskeYtelser),
        )

    val terskelVedSamordning = Opplysningstype.somDesimaltall("Hvor mange prosent av G skal brukes som terskel ved samordning")
    val treProsentG = Opplysningstype.somBeløp("Beløp tilsvarende nedre terskel av G")
    val skalSamordnesUtenforFolketrygden = Opplysningstype.somBoolsk("Skal samordnes med ytelser utenfor folketrygden")

    val regelsett =
        Regelsett("§ 4-26.Samordning med ytelser utenfor folketrygden") {
            regel(andreYtelser) { innhentes }

            regel(terskelVedSamordning) { oppslag(prøvingsdato) { 0.03 } }
            regel(treProsentG) { multiplikasjon(grunnbeløp, terskelVedSamordning) }

            regel(pensjonFraOffentligTjenestepensjonsordning) { oppslag(prøvingsdato) { false } }
            regel(redusertUførepensjon) { oppslag(prøvingsdato) { false } }
            regel(vartpenger) { oppslag(prøvingsdato) { false } }
            regel(ventelønn) { oppslag(prøvingsdato) { false } }
            regel(etterlønn) { oppslag(prøvingsdato) { false } }
            regel(garantilottGFF) { oppslag(prøvingsdato) { false } }

            regel(andreØkonomiskeYtelser) { innhentes }

            regel(skalSamordnesUtenforFolketrygden) {
                enAv(
                    andreYtelser,
                    pensjonFraOffentligTjenestepensjonsordning,
                    redusertUførepensjon,
                    vartpenger,
                    ventelønn,
                    etterlønn,
                    garantilottGFF,
                    andreØkonomiskeYtelser,
                )
            }
        }

    val ønsketResultat = listOf(skalSamordnesUtenforFolketrygden)

    val YtelserUtenforFolketrygdenKontrollPunkt =
        Kontrollpunkt(sjekker = Avklaringspunkter.YtelserUtenforFolketrygden) { opplysninger ->
            opplysninger.har(andreYtelser) && opplysninger.finnOpplysning(andreYtelser).verdi
        }
}
