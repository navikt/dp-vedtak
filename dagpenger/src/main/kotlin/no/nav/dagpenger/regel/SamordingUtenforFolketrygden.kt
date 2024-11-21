package no.nav.dagpenger.regel

import no.nav.dagpenger.avklaring.Kontrollpunkt
import no.nav.dagpenger.opplysning.Opplysningstype
import no.nav.dagpenger.opplysning.Regelsett
import no.nav.dagpenger.opplysning.id
import no.nav.dagpenger.opplysning.regel.divisjon
import no.nav.dagpenger.opplysning.regel.enAv
import no.nav.dagpenger.opplysning.regel.høyesteAv
import no.nav.dagpenger.opplysning.regel.innhentes
import no.nav.dagpenger.opplysning.regel.multiplikasjon
import no.nav.dagpenger.opplysning.regel.oppslag
import no.nav.dagpenger.opplysning.regel.substraksjonTilNull
import no.nav.dagpenger.opplysning.verdier.Beløp
import no.nav.dagpenger.regel.Behov.AndreØkonomiskeYtelser
import no.nav.dagpenger.regel.Behov.OppgittAndreYtelserUtenforNav
import no.nav.dagpenger.regel.Samordning.samordnetDagsats
import no.nav.dagpenger.regel.Søknadstidspunkt.prøvingsdato
import no.nav.dagpenger.regel.fastsetting.Dagpengegrunnlag.grunnbeløpForDagpengeGrunnlag
import no.nav.dagpenger.regel.fastsetting.DagpengenesStørrelse.arbeidsdagerPerUke

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
    val minsteUkessatsEtterSamordning = Opplysningstype.somBeløp("Beløp tilsvarende nedre terskel av G")
    val skalSamordnesUtenforFolketrygden = Opplysningstype.somBoolsk("Skal samordnes med ytelser utenfor folketrygden")

    val sumDetSkalSamordnesMot = Opplysningstype.somBeløp("Penger fra utsida som skal samordnes mot")
    val denRosaStrekenUnderDenRøde = Opplysningstype.somBeløp("Rosa strek")
    val redusertUkessatsEtterSamordning = Opplysningstype.somBeløp("Redusert")
    val samordnetUkessats = Opplysningstype.somBeløp("Samordnet")
    val enAnnenUkessats = Opplysningstype.somBeløp("Samordnet ukessats uten barnetillegg")
    val dobbeltSåSamordnetDagsats = Opplysningstype.somBeløp("Samordnet innvending og utvending dagsats uten barnetillegg")

    val regelsett =
        Regelsett("§ 4-26.Samordning med ytelser utenfor folketrygden") {
            regel(andreYtelser) { innhentes }

            regel(terskelVedSamordning) { oppslag(prøvingsdato) { 0.03 } }
            regel(minsteUkessatsEtterSamordning) { multiplikasjon(grunnbeløpForDagpengeGrunnlag, terskelVedSamordning) }

            regel(pensjonFraOffentligTjenestepensjonsordning) { oppslag(prøvingsdato) { false } }
            regel(redusertUførepensjon) { oppslag(prøvingsdato) { false } }
            regel(vartpenger) { oppslag(prøvingsdato) { false } }
            regel(ventelønn) { oppslag(prøvingsdato) { false } }
            regel(etterlønn) { oppslag(prøvingsdato) { false } }
            regel(garantilottGFF) { oppslag(prøvingsdato) { false } }

            regel(andreØkonomiskeYtelser) { innhentes }

            regel(sumDetSkalSamordnesMot) { oppslag(prøvingsdato) { Beløp(0.0) } }
            regel(denRosaStrekenUnderDenRøde) { substraksjonTilNull(minsteUkessatsEtterSamordning, sumDetSkalSamordnesMot) }

            regel(enAnnenUkessats) { multiplikasjon(samordnetDagsats, arbeidsdagerPerUke) }
            regel(redusertUkessatsEtterSamordning) { substraksjonTilNull(enAnnenUkessats, sumDetSkalSamordnesMot) }

            regel(samordnetUkessats) { høyesteAv(redusertUkessatsEtterSamordning, denRosaStrekenUnderDenRøde) }

            regel(dobbeltSåSamordnetDagsats) { divisjon(samordnetUkessats, arbeidsdagerPerUke) }

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
