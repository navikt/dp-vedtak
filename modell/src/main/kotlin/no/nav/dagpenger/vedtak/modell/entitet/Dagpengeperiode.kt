package no.nav.dagpenger.vedtak.modell.entitet

class Dagpengeperiode(private val antallUker: Int) {
    infix operator fun times(faktor: Int) = antallUker * faktor
}
