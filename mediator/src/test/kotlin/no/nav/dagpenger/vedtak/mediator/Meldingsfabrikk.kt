package no.nav.dagpenger.vedtak.mediator

import java.time.LocalDate
import java.util.UUID

object Meldingsfabrikk {

    fun dagpengerInnvilgetJson(rettighetstype: String = "Ordinær", ident: String = "12345123451") =
        //language=JSON
        """
        {        
          "@event_name": "søknad_behandlet_hendelse",
          "ident" : "$ident",
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

    fun dagpengerAvslåttJson(ident: String = "12345123451") =
        //language=JSON
        """
        {        
          "@event_name": "søknad_behandlet_hendelse",
          "ident" : "$ident",
          "behandlingId": "${UUID.randomUUID()}",
          "Virkningsdato": "${LocalDate.now()}",
          "innvilget": false
        } 
        """.trimIndent()

    fun rapporteringBehandletJson(ident: String = "12345123451") =
        //language=JSON
        """
        {        
          "@event_name": "rapportering_behandlet_hendelse",
          "ident" : "$ident",
          "behandlingId": "${UUID.randomUUID()}",
          "periodeId": "${UUID.randomUUID()}",
          "virkningsdato": "${LocalDate.now()}",
          "innvilget": false,
          "dager": [
            {"dato":"${LocalDate.now()}", "timer":0, "fravær":false},
            {"dato":"${LocalDate.now().plusDays(1)}", "timer":0, "fravær":false},
            {"dato":"${LocalDate.now().plusDays(2)}", "timer":0, "fravær":false}          
          ]
        } 
        """.trimIndent()

    fun iverksettJson(vedtakId: UUID = UUID.fromString("408f11d9-4be8-450a-8b7a-c2f3f9811859")) =
        //language=JSON
        """{
        "@event_name": "behov",
        "@behovId": "fe6fb8ee-cbc7-46bf-a5d7-fb9b57b279c4",
        "@behov": [
          "Iverksett"
        ],
        "ident": "12345678911",
        "iverksettingId": "0b853210-cc2b-45d8-9c35-72b39fa1d7f3",
        "vedtakId": "$vedtakId",
        "tilstand": "Mottatt",
        "Iverksett": {
          "vedtakId": "408f11d9-4be8-450a-8b7a-c2f3f9811859",
          "behandlingId": "0aaa66b9-35c2-4398-aca0-d1d0a9465292",
          "vedtakstidspunkt": "2019-08-24T14:15:22",
          "virkningsdato": "2019-08-24",
          "utfall": "Innvilget"
        },
        "behandlingId": "0aaa66b9-35c2-4398-aca0-d1d0a9465292",
        "vedtakstidspunkt": "2019-08-24T14:15:22",
        "virkningsdato": "2019-08-24",
        "utfall": "Innvilget",
        "@id": "2a49bcc2-2101-435d-83cc-2cc7905041b9",
        "@opprettet": "2023-05-11T10:02:10.0279828",
        "system_read_count": 0,
        "system_participating_services": [
          {
            "id": "2a49bcc2-2101-435d-83cc-2cc7905041b9",
            "time": "2023-05-11T10:02:10.027982800"
          }
        ]
      }"""
}
