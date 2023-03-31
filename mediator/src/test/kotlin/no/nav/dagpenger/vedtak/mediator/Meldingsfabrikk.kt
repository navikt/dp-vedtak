package no.nav.dagpenger.vedtak.mediator

import java.time.LocalDate
import java.util.UUID

object Meldingsfabrikk {

    fun søknadInnvilgetJson() =
        //language=JSON
        """
        {        
          "@event_name": "søknad_innvilget_hendelse",
          "behandlingId": "${UUID.randomUUID()}",
          "virkningsdato": "${LocalDate.now()}",
          "dagpengerettighet": "Ordinær",
          "dagsats": "500",
          "grunnlag": "500000",
          "stønadsperiode": "52",
          "vanligArbeidstidPerDag": "8",
          "antallVentedager": "3",
          "ident" : "12345123451"
        } 
        """.trimIndent()
}
