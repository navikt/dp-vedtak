package no.nav.dagpenger.features

import io.cucumber.java8.No
import java.time.LocalDate
import java.time.format.DateTimeFormatter

class DagpengeperiodeSteg : No {
    init {

        Gitt("at søker har har rett til dagpenger fra {string}") { dato: String ->
            val dato = LocalDate.parse(dato, DateTimeFormatter.ofPattern("dd.MM.yyyy"))
            throw io.cucumber.java8.PendingException()
        }
        Gitt("at søker har {string} siste 12 måneder") { inntekt: String ->
            // Write code here that turns the phrase above into concrete actions
            throw io.cucumber.java8.PendingException()
        }

        Gitt("at søker har {string} siste 36 måneder") { inntekt: String ->
            // Write code here that turns the phrase above into concrete actions
            throw io.cucumber.java8.PendingException()
        }
        Gitt("at søker oppfyller verneplikt {boolsk}") { verneplikt: Boolean ->
            // Write code here that turns the phrase above into concrete actions
            throw io.cucumber.java8.PendingException()
        }
        Så("skal søker ha {int} uker med dagpenger") { uker: Int ->
            // Write code here that turns the phrase above into concrete actions
            throw io.cucumber.java8.PendingException()
        }
    }
}
