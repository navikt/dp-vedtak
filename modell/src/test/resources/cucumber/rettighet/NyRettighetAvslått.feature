# language: no
Egenskap: Ny rettighet avslått

  Scenario:
    Gitt en ny hendelse om behandlet søknad
      | fødselsnummer | behandlingId | utfall | virkningsdato |
      | 12345678901   | 1            | false  | 14.12.2022    |
    Så skal bruker ha 1 vedtak
    Og vedtaket har virkningsdato "14.12.2022"