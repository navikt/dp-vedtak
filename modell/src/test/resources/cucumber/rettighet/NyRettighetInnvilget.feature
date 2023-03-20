# language: no
Egenskap: Ny rettighet innvilget


  Scenario: Innvilget vedtak fra 14.12.2022
    Gitt en ny hendelse om behandlet søknad
      | fødselsnummer | behandlingId | utfall | virkningsdato |
      | 12345678901   | 1            | true   | 14.12.2022    |
    Så skal bruker ha 1 vedtak
    Og vedtaket har virkningsdato "14.12.2022"
