package no.nav.dagpenger.vedtak.modell.rapportering

import no.nav.dagpenger.vedtak.modell.Person
import no.nav.dagpenger.vedtak.modell.hendelser.Rapporteringshendelse
import java.util.UUID

class Behandling(behandlingId: UUID, private val person: Person, private val behandlingssteg: Behandlingssteg) {

    constructor(person: Person) : this(UUID.randomUUID(), person, SjekkRammevedtak)

    fun håndter(rapporteringshendelse: Rapporteringshendelse) {
        behandlingssteg.håndter(rapporteringshendelse)
    }

    sealed class Behandlingssteg {
        // peke til hvilken paragraf eller forskrift
        open fun håndter(rapporteringshendelse: Rapporteringshendelse) {
            rapporteringshendelse.severe("Forventet ikke ${rapporteringshendelse.javaClass.simpleName} i ${this.javaClass.simpleName}")
        }
    }

    object SjekkRammevedtak : Behandlingssteg() {
        override fun håndter(rapporteringshendelse: Rapporteringshendelse) {
        }
    }
}
