package no.nav.dagpenger.regel

import no.nav.dagpenger.opplysning.LesbarOpplysninger
import no.nav.dagpenger.opplysning.Opplysningsformål.Bruker
import no.nav.dagpenger.opplysning.Opplysningstype
import no.nav.dagpenger.opplysning.Opplysningstype.Companion.aldriSynlig
import no.nav.dagpenger.opplysning.Regelsett
import no.nav.dagpenger.opplysning.id
import no.nav.dagpenger.opplysning.regel.alle
import no.nav.dagpenger.opplysning.regel.enAv
import no.nav.dagpenger.opplysning.regel.erSann
import no.nav.dagpenger.opplysning.regel.erUsann
import no.nav.dagpenger.opplysning.regel.innhentMed
import no.nav.dagpenger.opplysning.regel.oppslag
import no.nav.dagpenger.regel.Behov.TarUtdanningEllerOpplæring
import no.nav.dagpenger.regel.Søknadstidspunkt.prøvingsdato

object Utdanning {
    private val hvisTarUtdanning: (LesbarOpplysninger) -> Boolean = { it.erSann(tarUtdanning) }

    // § 4-6 første og andre avsnitt, Utdanning
    internal val tarUtdanning = Opplysningstype.somBoolsk("Tar utdanning eller opplæring?".id(TarUtdanningEllerOpplæring), Bruker)
    internal val godkjentUnntakForUtdanning = Opplysningstype.somBoolsk("Godkjent unntak for utdanning eller opplæring?")
    private val svartJaPåUtdanning =
        Opplysningstype.somBoolsk("Har svart ja på spørsmål om utdanning eller opplæring", synlig = aldriSynlig)
    private val svartNeiPåUtdanning =
        Opplysningstype.somBoolsk("Har svart nei på spørsmål om utdanning eller opplæring", synlig = aldriSynlig)

    private val oppfyllerKravetPåUnntak =
        Opplysningstype.somBoolsk("Oppfyller kravet på unntak for utdanning eller opplæring", synlig = hvisTarUtdanning)

    // Dagpengeforskriften § 4-3. Utdanning og opplæring
    //  § 4-3.(deltakelse i arbeidsmarkedstiltak)
    val deltakelseIArbeidsmarkedstiltak = Opplysningstype.somBoolsk("Deltar i arbeidsmarkedstiltak")

    // § 4-3a.(opplæring for innvandrere)
    val opplæringForInnvandrere = Opplysningstype.somBoolsk("Deltar i opplæring for innvandrere", synlig = hvisTarUtdanning)

    // § 4-3b.(grunnskoleopplæring, videregående opplæring og opplæring i grunnleggende ferdigheter)
    val grunnskoleopplæring =
        Opplysningstype.somBoolsk(
            "Deltar i grunnskoleopplæring, videregående opplæring og opplæring i grunnleggende ferdigheter",
            synlig = hvisTarUtdanning,
        )

    // § 4-3c.(høyere yrkesfaglig utdanning)
    val høyereYrkesfagligUtdanning = Opplysningstype.somBoolsk("Deltar i høyere yrkesfaglig utdanning", synlig = hvisTarUtdanning)

    // § 4-3d.(høyere utdanning)
    val høyereUtdanning = Opplysningstype.somBoolsk("Deltar i høyere utdanning", synlig = hvisTarUtdanning)

    // § 4-3e.(deltakelse på kurs mv
    val deltakelsePåKurs = Opplysningstype.somBoolsk("Deltar på kurs mv", synlig = hvisTarUtdanning)

    val kravTilUtdanning = Opplysningstype.somBoolsk("Krav til utdanning eller opplæring")

    val regelsett =
        Regelsett(
            folketrygden.hjemmel(
                kapittel = 4,
                paragraf = 6,
                tittel = "Dagpenger under utdanning, opplæring, etablering av egen virksomhet m.v",
                kortnavn = "4-6 Utdanning",
            ),
        ) {
            regel(tarUtdanning) { innhentMed() }

            // TODO: Legg til regler for å om kravet til utdanning skal vurderes
            regel(deltakelseIArbeidsmarkedstiltak) { oppslag(prøvingsdato) { false } }
            regel(opplæringForInnvandrere) { oppslag(prøvingsdato) { false } }
            regel(grunnskoleopplæring) { oppslag(prøvingsdato) { false } }
            regel(høyereYrkesfagligUtdanning) { oppslag(prøvingsdato) { false } }
            regel(høyereUtdanning) { oppslag(prøvingsdato) { false } }
            regel(deltakelsePåKurs) { oppslag(prøvingsdato) { false } }

            regel(godkjentUnntakForUtdanning) {
                enAv(
                    deltakelseIArbeidsmarkedstiltak,
                    opplæringForInnvandrere,
                    grunnskoleopplæring,
                    høyereYrkesfagligUtdanning,
                    høyereUtdanning,
                    deltakelsePåKurs,
                )
            }
            regel(svartJaPåUtdanning) { erSann(tarUtdanning) }
            regel(svartNeiPåUtdanning) { erUsann(tarUtdanning) }
            regel(oppfyllerKravetPåUnntak) { alle(svartJaPåUtdanning, godkjentUnntakForUtdanning) }

            utfall(kravTilUtdanning) { enAv(oppfyllerKravetPåUnntak, svartNeiPåUtdanning) }
        }
}
