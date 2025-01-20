package no.nav.dagpenger.regel

import no.nav.dagpenger.avklaring.Kontrollpunkt
import no.nav.dagpenger.opplysning.Opplysningssjekk
import no.nav.dagpenger.opplysning.Opplysningstype
import no.nav.dagpenger.opplysning.Opplysningstype.Companion.aldriSynlig
import no.nav.dagpenger.opplysning.Regelsett
import no.nav.dagpenger.opplysning.id
import no.nav.dagpenger.opplysning.regel.addisjon
import no.nav.dagpenger.opplysning.regel.enAv
import no.nav.dagpenger.opplysning.regel.innhentMed
import no.nav.dagpenger.opplysning.regel.oppslag
import no.nav.dagpenger.opplysning.regel.størreEnnEllerLik
import no.nav.dagpenger.opplysning.regel.substraksjon
import no.nav.dagpenger.opplysning.regel.substraksjonTilNull
import no.nav.dagpenger.opplysning.verdier.Beløp
import no.nav.dagpenger.regel.Behov.Foreldrepenger
import no.nav.dagpenger.regel.Behov.Omsorgspenger
import no.nav.dagpenger.regel.Behov.Opplæringspenger
import no.nav.dagpenger.regel.Behov.Pleiepenger
import no.nav.dagpenger.regel.Behov.Svangerskapspenger
import no.nav.dagpenger.regel.Behov.Sykepenger
import no.nav.dagpenger.regel.Behov.Uføre
import no.nav.dagpenger.regel.Samordning.skalSamordnes
import no.nav.dagpenger.regel.Søknadstidspunkt.prøvingsdato
import no.nav.dagpenger.regel.TapAvArbeidsinntektOgArbeidstid.beregnetArbeidstid
import no.nav.dagpenger.regel.fastsetting.DagpengenesStørrelse.dagsatsEtterNittiProsent
import no.nav.dagpenger.regel.fastsetting.DagpengenesStørrelse.harBarnetillegg

private val visesHvisSamordning: Opplysningssjekk = { it.erSann(skalSamordnes) }

/**
 * § 4-25.Samordning med reduserte ytelser fra folketrygden, eller redusert avtalefestet pensjon
 */
object Samordning {
    internal val sykepenger = Opplysningstype.somBoolsk("Sykepenger etter lovens kapittel 8".id(Sykepenger))
    internal val pleiepenger = Opplysningstype.somBoolsk("Pleiepenger etter lovens kapittel 9".id(Pleiepenger))
    internal val omsorgspenger = Opplysningstype.somBoolsk("Omsorgspenger etter lovens kapittel 9".id(Omsorgspenger))
    internal val opplæringspenger = Opplysningstype.somBoolsk("Opplæringspenger etter lovens kapittel 9".id(Opplæringspenger))
    internal val uføre = Opplysningstype.somBoolsk("Uføretrygd etter lovens kapittel 12".id(Uføre))
    internal val foreldrepenger = Opplysningstype.somBoolsk("Foreldrepenger etter lovens kapittel 14".id(Foreldrepenger))
    internal val svangerskapspenger = Opplysningstype.somBoolsk("Svangerskapspenger etter lovens kapittel 14".id(Svangerskapspenger))

    val sykepengerDagsats = Opplysningstype.somBeløp("Sykepenger dagsats", synlig = { it.erSann(sykepenger) })
    val pleiepengerDagsats = Opplysningstype.somBeløp("Pleiepenger dagsats", synlig = { it.erSann(pleiepenger) })
    val omsorgspengerDagsats = Opplysningstype.somBeløp("Omsorgspenger dagsats", synlig = { it.erSann(omsorgspenger) })
    val opplæringspengerDagsats = Opplysningstype.somBeløp("Opplæringspenger dagsats", synlig = { it.erSann(opplæringspenger) })
    val uføreDagsats = Opplysningstype.somBeløp("Uføre dagsats", synlig = { it.erSann(uføre) })
    val foreldrepengerDagsats = Opplysningstype.somBeløp("Foreldrepenger dagsats", synlig = { it.erSann(foreldrepenger) })
    val svangerskapspengerDagsats = Opplysningstype.somBeløp("Svangerskapspenger dagsats", synlig = { it.erSann(svangerskapspenger) })

    private val sumAndreYtelser = Opplysningstype.somBeløp("Sum andre ytelser", synlig = visesHvisSamordning)
    internal val skalSamordnes =
        Opplysningstype.somBoolsk("Medlem har reduserte ytelser fra folketrygden (Samordning)", synlig = aldriSynlig)

    internal val samordnetDagsats = Opplysningstype.somBeløp("Samordnet dagsats uten barnetillegg", synlig = visesHvisSamordning)
    private val kanUtbetale = Opplysningstype.somBoolsk("Samordnet dagsats er negativ eller 0", synlig = visesHvisSamordning)
    val samordnetArbeidstid =
        Opplysningstype.somDesimaltall("Antall timer arbeidstiden skal samordnes mot", synlig = visesHvisSamordning)
    val samordnetBeregnetArbeidstid =
        Opplysningstype.somDesimaltall("Samordnet fastsatt arbeidstid", synlig = visesHvisSamordning)
    internal val utfallEtterSamordning = Opplysningstype.somBoolsk("Utfall etter samordning", synlig = visesHvisSamordning)

    // Fulle dagpenger minus en/flere av reduserte ytelsene man mottar per samme dag (regnestykket)
    // avrundetDagsUtenBarnetillegg - sykepenger - pleiepenger - omsorgspenger - opplæringspenger - uføre - foreldrepenger - svangerskapspenger

    val regelsett =
        Regelsett(
            folketrygden.hjemmel(
                kapittel = 4,
                paragraf = 25,
                tittel = "Samordning med reduserte ytelser fra folketrygden, eller redusert avtalefestet pensjon",
                kortnavn = "4-25 Samordning reduserte ytelser",
            ),
        ) {
            regel(sykepenger) { innhentMed(prøvingsdato) }
            regel(pleiepenger) { innhentMed(prøvingsdato) }
            regel(omsorgspenger) { innhentMed(prøvingsdato) }
            regel(opplæringspenger) { innhentMed(prøvingsdato) }
            regel(foreldrepenger) { innhentMed(prøvingsdato) }
            regel(svangerskapspenger) { innhentMed(prøvingsdato) }

            // TODO: Hent uførestrygd og barnepenger fra pesys
            regel(uføre) { oppslag(prøvingsdato) { false } }

            regel(samordnetArbeidstid) { oppslag(prøvingsdato) { 0.0 } }
            regel(samordnetBeregnetArbeidstid) { substraksjon(beregnetArbeidstid, samordnetArbeidstid) }

            regel(sykepengerDagsats) { oppslag(prøvingsdato) { Beløp(0.0) } }
            regel(pleiepengerDagsats) { oppslag(prøvingsdato) { Beløp(0.0) } }
            regel(omsorgspengerDagsats) { oppslag(prøvingsdato) { Beløp(0.0) } }
            regel(opplæringspengerDagsats) { oppslag(prøvingsdato) { Beløp(0.0) } }
            regel(uføreDagsats) { oppslag(prøvingsdato) { Beløp(0.0) } }
            regel(svangerskapspengerDagsats) { oppslag(prøvingsdato) { Beløp(0.0) } }
            regel(foreldrepengerDagsats) { oppslag(prøvingsdato) { Beløp(0.0) } }
            regel(sumAndreYtelser) {
                addisjon(
                    sykepengerDagsats,
                    pleiepengerDagsats,
                    omsorgspengerDagsats,
                    opplæringspengerDagsats,
                    uføreDagsats,
                    foreldrepengerDagsats,
                    svangerskapspengerDagsats,
                )
            }

            regel(samordnetDagsats) { substraksjonTilNull(dagsatsEtterNittiProsent, sumAndreYtelser) }
            regel(kanUtbetale) { størreEnnEllerLik(dagsatsEtterNittiProsent, sumAndreYtelser) }

            regel(utfallEtterSamordning) { enAv(kanUtbetale, harBarnetillegg) }

            utfall(skalSamordnes) {
                enAv(
                    sykepenger,
                    pleiepenger,
                    omsorgspenger,
                    opplæringspenger,
                    uføre,
                    foreldrepenger,
                    svangerskapspenger,
                )
            }

            relevantHvis {
                it.har(skalSamordnes) && it.finnOpplysning(skalSamordnes).verdi
            }
        }

    val ønsketResultat = listOf(samordnetDagsats, skalSamordnes, utfallEtterSamordning)

    val SkalSamordnes =
        Kontrollpunkt(Avklaringspunkter.Samordning) {
            it.har(skalSamordnes) && it.finnOpplysning(skalSamordnes).verdi
        }
}
