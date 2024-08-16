package no.nav.dagpenger.regel.fastsetting

import no.nav.dagpenger.opplysning.Opplysningstype
import no.nav.dagpenger.opplysning.Regelsett
import no.nav.dagpenger.opplysning.TemporalCollection
import no.nav.dagpenger.opplysning.regel.addisjon
import no.nav.dagpenger.opplysning.regel.avrund
import no.nav.dagpenger.opplysning.regel.multiplikasjon
import no.nav.dagpenger.opplysning.regel.oppslag
import no.nav.dagpenger.opplysning.verdier.Beløp
import no.nav.dagpenger.regel.Søknadstidspunkt
import java.math.BigDecimal
import java.time.LocalDate

object DagpengensStørrelse {
    private val grunnlag = Dagpengegrunnlag.grunnlag

    // TODO: Hent antall barn fra søknaden. Hvordan skal saksbehandler godkjenne antall barn?
    internal val antallBarn = Opplysningstype.somHeltall("Antall barn")
    private val barnetillegetsStørrelse = Opplysningstype.somBeløp("Barnetilleggets størrelse")

    /**
     * 1. Hente barn fra søknad
     * 2. Saksbehandler vilkårprøver at en har rett til barnetillegg per barn
     * 3. == antall barn * barnetillegg
     */
    private val dekningsgrad = Opplysningstype.somDesimaltall("Dekningsgrad")
    val dagpengensStørrelse = Opplysningstype.somBeløp("Dagpengens størrelse")
    val avrundetDagpengensStørrelse = Opplysningstype.somBeløp("Dagpengens størrelse avrundet")
    val barnetillegg = Opplysningstype.somBeløp("Barnetillegg")
    val dagsatsMedBarn = Opplysningstype.somBeløp("Dagsats med barn")

    val regelsett =
        Regelsett("§ 4-12. Dagpengens størrelse (Sats)") {

            regel(barnetillegetsStørrelse) { oppslag(Søknadstidspunkt.søknadstidspunkt) { Barnetillegg.forDato(it) } }
            regel(dekningsgrad) {
                oppslag(Søknadstidspunkt.søknadstidspunkt) { DagpengensStørrelseFaktor.forDato(it) }
            } // 2,4% av grunnlag. TODO: Hent faktor fra konfiguasjon som er datostyrt
            regel(dagpengensStørrelse) { multiplikasjon(grunnlag, dekningsgrad) }
            regel(avrundetDagpengensStørrelse) { avrund(dagpengensStørrelse) }
            regel(barnetillegg) { multiplikasjon(barnetillegetsStørrelse, antallBarn) }
            regel(dagsatsMedBarn) { addisjon(avrundetDagpengensStørrelse, barnetillegg) }
        }
}

private object Barnetillegg {
    private val satser =
        TemporalCollection<BigDecimal>().apply {
            // Defineres her: https://lovdata.no/pro/#document/SF/forskrift/1998-09-16-890/%C2%A77-1
            put(LocalDate.MIN, BigDecimal(17))
            put(LocalDate.of(2023, 2, 1), BigDecimal(35))
            put(LocalDate.of(2024, 1, 1), BigDecimal(36))
        }

    fun forDato(regelverksdato: LocalDate) = Beløp(satser.get(regelverksdato))
}

private object DagpengensStørrelseFaktor {
    private val faktorer =
        TemporalCollection<Double>().apply {
            // Defineres her: https://lovdata.no/lov/1997-02-28-19/§4-12
            put(LocalDate.MIN, 0.0024)
        }

    fun forDato(regelverksdato: LocalDate) = faktorer.get(regelverksdato)
}
