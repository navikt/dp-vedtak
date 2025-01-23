package no.nav.dagpenger.regel

import no.nav.dagpenger.avklaring.Kontrollpunkt
import no.nav.dagpenger.opplysning.Opplysningstype
import no.nav.dagpenger.opplysning.Opplysningstype.Companion.aldriSynlig
import no.nav.dagpenger.opplysning.Regelsett
import no.nav.dagpenger.opplysning.regel.dato.sisteAv
import no.nav.dagpenger.opplysning.regel.innhentMed
import no.nav.dagpenger.opplysning.regel.innhentes
import no.nav.dagpenger.regel.Behov.Prøvingsdato
import no.nav.dagpenger.regel.Behov.Søknadsdato
import no.nav.dagpenger.regel.Behov.ØnskerDagpengerFraDato
import no.nav.dagpenger.regel.OpplysningsTyper.prøvingsdatoId
import no.nav.dagpenger.regel.OpplysningsTyper.søknadId
import no.nav.dagpenger.regel.OpplysningsTyper.søknadsdatoId
import no.nav.dagpenger.regel.OpplysningsTyper.søknadstidspunktId
import no.nav.dagpenger.regel.OpplysningsTyper.ønskerDagpengerFraDatoId

object Søknadstidspunkt {
    // § 3A-1.Søknadstidspunkt https://lovdata.no/forskrift/1998-09-16-890/§3a-1
    val søknadsdato = Opplysningstype.dato(søknadsdatoId, "Søknadsdato", behovId = Søknadsdato)
    val ønsketdato = Opplysningstype.dato(ønskerDagpengerFraDatoId, "Ønsker dagpenger fra dato", behovId = ØnskerDagpengerFraDato)

    val søknadstidspunkt = Opplysningstype.dato(søknadstidspunktId, "Søknadstidspunkt", synlig = aldriSynlig)

    val prøvingsdato = Opplysningstype.dato(prøvingsdatoId, "Prøvingsdato", behovId = Prøvingsdato)
    val søknadIdOpplysningstype = Opplysningstype.tekst(søknadId, "søknadId")

    val regelsett =
        Regelsett(
            forskriftTilFolketrygden.hjemmel(3, 1, "Søknadstidspunkt", "§3A-1 Søknadstidspunkt"),
        ).apply {
            regel(søknadIdOpplysningstype) { innhentes }
            regel(søknadsdato) { innhentMed(søknadIdOpplysningstype) }
            regel(ønsketdato) { innhentMed(søknadIdOpplysningstype) }
            regel(søknadstidspunkt) { sisteAv(søknadsdato, ønsketdato) }
            regel(prøvingsdato) { sisteAv(søknadstidspunkt) }
        }

    val SøknadstidspunktForLangtFramITid =
        Kontrollpunkt(Avklaringspunkter.SøknadstidspunktForLangtFramITid) {
            it.har(søknadstidspunkt) &&
                it.finnOpplysning(søknadstidspunkt).verdi.isAfter(
                    it.finnOpplysning(søknadsdato).verdi.plusDays(14),
                )
        }
}
