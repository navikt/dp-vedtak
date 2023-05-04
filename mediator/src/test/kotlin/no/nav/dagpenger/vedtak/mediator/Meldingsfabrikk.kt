package no.nav.dagpenger.vedtak.mediator

import java.time.LocalDate
import java.util.UUID

object Meldingsfabrikk {

    fun dagpengerInnvilgetJson(rettighetstype: String = "Ordinær") =
        //language=JSON
        """
        {        
          "@event_name": "søknad_behandlet_hendelse",
          "ident" : "12345123451",
          "behandlingId": "${UUID.randomUUID()}",
          "Virkningsdato": "${LocalDate.now()}",
          "innvilget": true,
          "Rettighetstype": "$rettighetstype",
          "Dagsats": "500",
          "Grunnlag": "500000",
          "Periode": "52",
          "Fastsatt vanlig arbeidstid": "8",
          "egenandel": "1500",
          "barnetillegg" : [ {
              "fødselsdato" : "2012-03-03"
          }, {
              "fødselsdato" : "2015-03-03"
          }]
           
        } 
        """.trimIndent()

    fun dagpengerAvslåttJson() =
        //language=JSON
        """
        {        
          "@event_name": "søknad_behandlet_hendelse",
          "ident" : "12345123451",
          "behandlingId": "${UUID.randomUUID()}",
          "Virkningsdato": "${LocalDate.now()}",
          "innvilget": false
        } 
        """.trimIndent()
}
