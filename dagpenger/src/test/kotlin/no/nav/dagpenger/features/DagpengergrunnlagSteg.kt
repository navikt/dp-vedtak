package no.nav.dagpenger.features

import io.cucumber.datatable.DataTable
import io.cucumber.java8.No
import io.cucumber.java8.PendingException

class DagpengergrunnlagSteg : No {
    init {
        Gitt("at verneplikt for grunnlag er satt {boolsk}") { verneplikt: Boolean ->
            // Write code here that turns the phrase above into concrete actions
            throw io.cucumber.java8.PendingException()
        }

        Gitt("at inntekt for grunnlag er") { dataTable: DataTable? ->
            throw PendingException()
        }

        Så("beregnet utfall være {string} og {string}") { avkortet: String, uavkortet: String ->
            // Write code here that turns the phrase above into concrete actions
            throw io.cucumber.java8.PendingException()
        }
    }
}
