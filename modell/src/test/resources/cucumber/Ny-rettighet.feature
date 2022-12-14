# language: no
Egenskap: ny rettighet
  Scenario: mottaker har rett til dagpenger
    Gitt at bruker er gitt dagpenger
      | Fødselsnummer  | BehandlingId | Innvilget      | Fra og med dato  | Sats | Periode |
      | 12345678901    | 1            | true           | 14.12.2022       | 488  | 52      |
    Så har bruker vedtak i vedtakhistorikken
    Når bruker rapporterer om dager
      |15.12.2022|
      |16.12.2022|
      |17.12.2022|
      |18.12.2022|
    Så skal bruker få utbetalt for dager hen har jobbet




