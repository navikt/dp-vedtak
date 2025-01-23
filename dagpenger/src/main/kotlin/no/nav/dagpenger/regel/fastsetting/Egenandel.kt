package no.nav.dagpenger.regel.fastsetting

import no.nav.dagpenger.opplysning.Opplysningstype.Companion.aldriSynlig
import no.nav.dagpenger.opplysning.Opplysningstype.Companion.beløp
import no.nav.dagpenger.opplysning.Opplysningstype.Companion.desimaltall
import no.nav.dagpenger.opplysning.Regelsett
import no.nav.dagpenger.opplysning.RegelsettType
import no.nav.dagpenger.opplysning.regel.multiplikasjon
import no.nav.dagpenger.opplysning.regel.oppslag
import no.nav.dagpenger.regel.OpplysningsTyper.AntallDagsatsForEgenandelId
import no.nav.dagpenger.regel.OpplysningsTyper.EgenandelId
import no.nav.dagpenger.regel.Søknadstidspunkt.prøvingsdato
import no.nav.dagpenger.regel.folketrygden

object Egenandel {
    val egenandel = beløp(EgenandelId, "Egenandel")
    private val sats = DagpengenesStørrelse.dagsatsEtterSamordningMedBarnetillegg
    private val faktor = desimaltall(AntallDagsatsForEgenandelId, "Antall dagsats for egenandel", synlig = aldriSynlig)

    val regelsett =
        Regelsett(
            folketrygden.hjemmel(4, 9, "Egenandel", "4-9 Egenandel"),
            RegelsettType.Fastsettelse,
        ) {
            regel(faktor) { oppslag(prøvingsdato) { 3.0 } }
            regel(egenandel) { multiplikasjon(sats, faktor) }
        }

    val ønsketResultat = listOf(egenandel)
}
