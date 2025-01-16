package no.nav.dagpenger.regel.fastsetting

import no.nav.dagpenger.opplysning.Opplysningstype
import no.nav.dagpenger.opplysning.Regelsett
import no.nav.dagpenger.opplysning.RegelsettType
import no.nav.dagpenger.opplysning.regel.hvisSannMedResultat
import no.nav.dagpenger.opplysning.regel.multiplikasjon
import no.nav.dagpenger.opplysning.regel.oppslag
import no.nav.dagpenger.opplysning.regel.størreEnn
import no.nav.dagpenger.opplysning.verdier.Beløp
import no.nav.dagpenger.regel.Søknadstidspunkt.prøvingsdato
import no.nav.dagpenger.regel.Verneplikt.oppfyllerKravetTilVerneplikt
import no.nav.dagpenger.regel.fastsetting.Dagpengegrunnlag.dagpengegrunnlag
import no.nav.dagpenger.regel.fastsetting.Dagpengegrunnlag.grunnbeløpForDagpengeGrunnlag
import no.nav.dagpenger.regel.fastsetting.VernepliktFastsetting.grunnlagForVernepliktErGunstigst
import no.nav.dagpenger.regel.fastsetting.VernepliktFastsetting.vernepliktFastsattVanligArbeidstid
import no.nav.dagpenger.regel.fastsetting.VernepliktFastsetting.vernepliktGrunnlag
import no.nav.dagpenger.regel.fastsetting.VernepliktFastsetting.vernepliktPeriode
import no.nav.dagpenger.regel.folketrygden

object VernepliktFastsetting {
    private val antallG = Opplysningstype.somDesimaltall("Antall G som gis som grunnlag ved verneplikt")
    internal val vernepliktGrunnlag = Opplysningstype.somBeløp("Grunnlag for gis ved verneplikt")
    val vernepliktPeriode = Opplysningstype.somHeltall("Periode som gis ved verneplikt")
    internal val vernepliktFastsattVanligArbeidstid = Opplysningstype.somDesimaltall("Fastsatt vanlig arbeidstid for verneplikt")
    internal val grunnlagHvisVerneplikt = Opplysningstype.somBeløp("Grunnlag for verneplikt hvis kravet er oppfylt")
    internal val grunnlagUtenVerneplikt = Opplysningstype.somBeløp("Grunnlag for verneplikt hvis kravet ikke er oppfylt")
    val grunnlagForVernepliktErGunstigst = Opplysningstype.somBoolsk("Grunnlaget for verneplikt er høyere enn dagpengegrunnlaget")

    val regelsett =
        Regelsett(
            folketrygden.hjemmel(4, 19, "Dagpenger etter avtjent verneplikt", "4-19 Dagpenger ved verneplikt"),
            RegelsettType.Fastsettelse,
        ) {
            regel(antallG) { oppslag(prøvingsdato) { 3.0 } }
            regel(vernepliktGrunnlag) { multiplikasjon(grunnbeløpForDagpengeGrunnlag, antallG) }
            regel(vernepliktPeriode) { oppslag(prøvingsdato) { 26 } }
            regel(vernepliktFastsattVanligArbeidstid) { oppslag(prøvingsdato) { 37.5 } }

            regel(grunnlagUtenVerneplikt) { oppslag(prøvingsdato) { Beløp(0) } }

            // Setter grunnlag avhengig av om bruker oppfyller kravet til verneplikt (0G eller 3G)
            regel(grunnlagHvisVerneplikt) { hvisSannMedResultat(oppfyllerKravetTilVerneplikt, vernepliktGrunnlag, grunnlagUtenVerneplikt) }

            // Kriteriet om vi skal bruke grunnlag og FVA fra verneplikt eller dagpengegrunnlag
            regel(grunnlagForVernepliktErGunstigst) { størreEnn(grunnlagHvisVerneplikt, dagpengegrunnlag) }

            relevantHvis {
                it.har(oppfyllerKravetTilVerneplikt) && it.finnOpplysning(oppfyllerKravetTilVerneplikt).verdi
            }
        }

    val ønsketResultat =
        listOf(
            vernepliktGrunnlag,
            vernepliktPeriode,
            vernepliktFastsattVanligArbeidstid,
            grunnlagForVernepliktErGunstigst,
        )
}
