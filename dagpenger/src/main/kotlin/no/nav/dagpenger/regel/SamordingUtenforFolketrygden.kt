package no.nav.dagpenger.regel

import no.nav.dagpenger.avklaring.Kontrollpunkt
import no.nav.dagpenger.opplysning.Opplysningstype
import no.nav.dagpenger.opplysning.Regelsett
import no.nav.dagpenger.opplysning.id
import no.nav.dagpenger.opplysning.regel.divisjon
import no.nav.dagpenger.opplysning.regel.enAv
import no.nav.dagpenger.opplysning.regel.høyesteAv
import no.nav.dagpenger.opplysning.regel.innhentes
import no.nav.dagpenger.opplysning.regel.inntekt.sumAv
import no.nav.dagpenger.opplysning.regel.minstAv
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

    private val pensjonFraOffentligTjenestepensjonsordning =
        Opplysningstype.somBoolsk(
            "Mottar pensjon fra en offentlig tjenestepensjonsordning",
        )
    private val redusertUførepensjon = Opplysningstype.somBoolsk("Mottar redusert uførepensjon fra offentlig pensjonsordning")
    private val vartpenger = Opplysningstype.somBoolsk("Mottar vartpenger")
    private val ventelønn = Opplysningstype.somBoolsk("Mottar ventelønn")
    private val etterlønn = Opplysningstype.somBoolsk("Mottar etterlønn")
    private val garantilottGFF = Opplysningstype.somBoolsk("Mottar garantilott fra Garantikassen for fiskere.")

    val pensjonFraOffentligTjenestepensjonsordningBeløp =
        Opplysningstype.somBeløp(
            "Pensjon fra en offentlig tjenestepensjonsordning beløp",
        )
    val redusertUførepensjonBeløp = Opplysningstype.somBeløp("Uførepensjon fra offentlig pensjonsordning beløp")
    val vartpengerBeløp = Opplysningstype.somBeløp("Vartpenger beløp")
    val ventelønnBeløp = Opplysningstype.somBeløp("Ventelønn beløp")
    val etterlønnBeløp = Opplysningstype.somBeløp("Etterlønn beløp")
    val garantilottGFFBeløp = Opplysningstype.somBeløp("Garantilott fra Garantikassen for fiskere beløp")

    val andreØkonomiskeYtelser =
        Opplysningstype.somBoolsk(
            "Mottar andre økonomiske ytelser fra arbeidsgiver eller tidligere arbeidsgiver enn lønn".id(AndreØkonomiskeYtelser),
        )

    private val terskelVedSamordning = Opplysningstype.somDesimaltall("Hvor mange prosent av G skal brukes som terskel ved samordning")
    val nedreGrenseForSamordning = Opplysningstype.somBeløp("Beløp tilsvarende nedre terskel av G")
    val skalSamordnesUtenforFolketrygden = Opplysningstype.somBoolsk("Skal samordnes med ytelser utenfor folketrygden")

    val sumAvYtelserUtenforFolketrygden = Opplysningstype.somBeløp("Sum av ytelser utenfor folketrygden")
    val samordnetUkessatsUtenBarnetillegg = Opplysningstype.somBeløp("Samordnet ukessats uten barnetillegg")
    private val minsteMuligeUkessats = Opplysningstype.somBeløp("Minste mulige ukessats som som kan brukes")
    private val samordnetUkessatsUtenforFolketrygden = Opplysningstype.somBeløp("Ukessats trukket ned for ytelser utenfor folketrygden")
    val samordnetUkessats = Opplysningstype.somBeløp("Samordnet ukessats med ytelser utenfor folketrygden")
    val dagsatsSamordnetUtenforFolketrygden = Opplysningstype.somBeløp("Dagsats uten barnetillegg samordnet")

    val regelsett =
        Regelsett(
            folketrygden.hjemmel(4, 26, "Samordning med ytelser utenfor folketrygden", "4-26 Samordning utenfor folketrygden"),
        ) {
            regel(andreYtelser) { innhentes }

            regel(pensjonFraOffentligTjenestepensjonsordning) { oppslag(prøvingsdato) { false } }
            regel(redusertUførepensjon) { oppslag(prøvingsdato) { false } }
            regel(vartpenger) { oppslag(prøvingsdato) { false } }
            regel(ventelønn) { oppslag(prøvingsdato) { false } }
            regel(etterlønn) { oppslag(prøvingsdato) { false } }
            regel(garantilottGFF) { oppslag(prøvingsdato) { false } }

            regel(andreØkonomiskeYtelser) { innhentes }

            regel(pensjonFraOffentligTjenestepensjonsordningBeløp) { oppslag(prøvingsdato) { Beløp(0.0) } }
            regel(redusertUførepensjonBeløp) { oppslag(prøvingsdato) { Beløp(0.0) } }
            regel(vartpengerBeløp) { oppslag(prøvingsdato) { Beløp(0.0) } }
            regel(ventelønnBeløp) { oppslag(prøvingsdato) { Beløp(0.0) } }
            regel(etterlønnBeløp) { oppslag(prøvingsdato) { Beløp(0.0) } }
            regel(garantilottGFFBeløp) { oppslag(prøvingsdato) { Beløp(0.0) } }

            regel(sumAvYtelserUtenforFolketrygden) {
                sumAv(
                    pensjonFraOffentligTjenestepensjonsordningBeløp,
                    redusertUførepensjonBeløp,
                    vartpengerBeløp,
                    ventelønnBeløp,
                    etterlønnBeløp,
                    garantilottGFFBeløp,
                )
            }

            regel(terskelVedSamordning) { oppslag(prøvingsdato) { 0.03 } }
            regel(nedreGrenseForSamordning) { multiplikasjon(grunnbeløpForDagpengeGrunnlag, terskelVedSamordning) }

            regel(samordnetUkessatsUtenBarnetillegg) { multiplikasjon(samordnetDagsats, arbeidsdagerPerUke) }

            regel(minsteMuligeUkessats) { minstAv(samordnetUkessatsUtenBarnetillegg, nedreGrenseForSamordning) }
            regel(samordnetUkessatsUtenforFolketrygden) {
                substraksjonTilNull(samordnetUkessatsUtenBarnetillegg, sumAvYtelserUtenforFolketrygden)
            }
            regel(samordnetUkessats) { høyesteAv(minsteMuligeUkessats, samordnetUkessatsUtenforFolketrygden) }

            regel(dagsatsSamordnetUtenforFolketrygden) { divisjon(samordnetUkessats, arbeidsdagerPerUke) }

            utfall(skalSamordnesUtenforFolketrygden) {
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

    val ønsketResultat = listOf(skalSamordnesUtenforFolketrygden, dagsatsSamordnetUtenforFolketrygden)

    val YtelserUtenforFolketrygdenKontroll =
        Kontrollpunkt(sjekker = Avklaringspunkter.YtelserUtenforFolketrygden) { opplysninger ->
            opplysninger.har(skalSamordnesUtenforFolketrygden) && opplysninger.finnOpplysning(skalSamordnesUtenforFolketrygden).verdi
        }
}
