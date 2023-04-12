package no.nav.dagpenger.vedtak.mediator

import java.time.LocalDate
import java.util.UUID

object Meldingsfabrikk {

    fun søknadInnvilgetJson() =
        //language=JSON
        """
        {        
          "@event_name": "søknad_behandlet_hendelse",
          "ident" : "12345123451",
          "behandlingId": "${UUID.randomUUID()}",
          "virkningsdato": "${LocalDate.now()}",
          "innvilget": true,
          "dagpengerettighet": "Ordinær",
          "dagsats": "500",
          "grunnlag": "500000",
          "stønadsperiode": "52",
          "vanligArbeidstidPerDag": "8",
          "antallVentedager": "3",
          "barnetillegg" : [ {
              "fødselsdato" : "2012-03-03"
          }, {
              "fødselsdato" : "2015-03-03"
          }]
           
        } 
        """.trimIndent()
}
