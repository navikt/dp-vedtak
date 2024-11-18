package no.nav.dagpenger.regel

import no.nav.dagpenger.avklaring.Kontrollpunkt
import no.nav.dagpenger.opplysning.Opplysningstype
import no.nav.dagpenger.opplysning.Regelsett
import no.nav.dagpenger.opplysning.id
import no.nav.dagpenger.opplysning.regel.enAv
import no.nav.dagpenger.opplysning.regel.innhentMed
import no.nav.dagpenger.opplysning.regel.oppslag
import no.nav.dagpenger.opplysning.regel.substraksjon
import no.nav.dagpenger.opplysning.verdier.Beløp
import no.nav.dagpenger.regel.Behov.Foreldrepenger
import no.nav.dagpenger.regel.Behov.Omsorgspenger
import no.nav.dagpenger.regel.Behov.Opplæringspenger
import no.nav.dagpenger.regel.Behov.Pleienger
import no.nav.dagpenger.regel.Behov.Svangerskapspenger
import no.nav.dagpenger.regel.Behov.Sykepenger
import no.nav.dagpenger.regel.Behov.Uføre
import no.nav.dagpenger.regel.Søknadstidspunkt.prøvingsdato
import no.nav.dagpenger.regel.fastsetting.DagpengenesStørrelse

/**
 * § 4-25.Samordning med reduserte ytelser fra folketrygden, eller redusert avtalefestet pensjon
 */
object Samordning {
    val sykepenger = Opplysningstype.somBoolsk("Sykepenger etter lovens kapittel 8".id(Sykepenger))
    val pleiepenger = Opplysningstype.somBoolsk("Pleiepenger etter lovens kapittel 9".id(Pleienger))
    val omsorgspenger = Opplysningstype.somBoolsk("Omsorgspenger etter lovens kapittel 9".id(Omsorgspenger))
    val opplæringspenger = Opplysningstype.somBoolsk("Opplæringspenger etter lovens kapittel 9".id(Opplæringspenger))
    val uføre = Opplysningstype.somBoolsk("Uføretrygd etter lovens kapittel 12".id(Uføre))
    val foreldrepenger = Opplysningstype.somBoolsk("Foreldrepenger etter lovens kapittel 14".id(Foreldrepenger))
    val svangerskapspenger = Opplysningstype.somBoolsk("Svangerskapspenger etter lovens kapittel 14".id(Svangerskapspenger))
    val skalSamordnes = Opplysningstype.somBoolsk("For medlem som har følgende reduserte ytelser fra folketrygden samme dag")

    val sykepengerDagsats = Opplysningstype.somBeløp("Sykepenger dagsats")
    val pleiepengerDagsats = Opplysningstype.somBeløp("Pleiepenger dagsats")
    val omsorgspengerDagsats = Opplysningstype.somBeløp("Omsorgspenger dagsats")
    val opplæringspengerDagsats = Opplysningstype.somBeløp("Opplæringspenger dagsats")
    val uføreDagsats = Opplysningstype.somBeløp("Uføre dagsats")
    val foreldrepengerDagsats = Opplysningstype.somBeløp("Foreldrepenger dagsats")
    val svangerskapspengerDagsats = Opplysningstype.somBeløp("Svangerskapspenger dagsats")

    val avrundetDagsUtenBarnetillegg = DagpengenesStørrelse.avrundetDagsUtenBarnetillegg
    val samordnetDagsats = Opplysningstype.somBeløp("Samordnet dagsats")

    // Fulle dagpenger minus en/flere av reduserte ytelsene man mottar per samme dag (regnestykket)
    // avrundetDagsUtenBarnetillegg - sykepenger - pleiepenger - omsorgspenger - opplæringspenger - uføre - foreldrepenger - svangerskapspenger

    val regelsett =
        Regelsett(
            "§ 4-25.Samordning med reduserte ytelser fra folketrygden, eller redusert avtalefestet pensjon",
        ) {
            regel(sykepenger) { innhentMed(prøvingsdato) }
            regel(pleiepenger) { innhentMed(prøvingsdato) }
            regel(omsorgspenger) { innhentMed(prøvingsdato) }
            regel(opplæringspenger) { innhentMed(prøvingsdato) }
            regel(uføre) { oppslag(prøvingsdato) { false } }
            regel(foreldrepenger) { innhentMed(prøvingsdato) }
            regel(svangerskapspenger) { innhentMed(prøvingsdato) }
            regel(sykepengerDagsats) { oppslag(prøvingsdato) { Beløp(0.0) } }
            regel(pleiepengerDagsats) { oppslag(prøvingsdato) { Beløp(0.0) } }
            regel(omsorgspengerDagsats) { oppslag(prøvingsdato) { Beløp(0.0) } }
            regel(opplæringspengerDagsats) { oppslag(prøvingsdato) { Beløp(0.0) } }
            regel(uføreDagsats) { oppslag(prøvingsdato) { Beløp(0.0) } }
            regel(svangerskapspengerDagsats) { oppslag(prøvingsdato) { Beløp(0.0) } }
            regel(foreldrepengerDagsats) { oppslag(prøvingsdato) { Beløp(0.0) } }
            regel(samordnetDagsats) {
                substraksjon(
                    avrundetDagsUtenBarnetillegg,
                    sykepengerDagsats,
                    pleiepengerDagsats,
                    omsorgspengerDagsats,
                    opplæringspengerDagsats,
                    uføreDagsats,
                    foreldrepengerDagsats,
                    svangerskapspengerDagsats,
                )
            }

            regel(skalSamordnes) {
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
        }

    val SkalSamordnes =
        Kontrollpunkt(Avklaringspunkter.Samordnes) {
            it.har(skalSamordnes)
        }
}
