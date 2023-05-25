package no.nav.dagpenger.vedtak.modell.entitet

class Dagpengeperiode(private val antallUker: Int) {
    fun tilStønadsdager() = Stønadsdager(antallUker * 5)
}
