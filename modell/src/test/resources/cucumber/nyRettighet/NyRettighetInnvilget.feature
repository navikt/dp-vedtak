# language: no
Egenskap: Ny rettighet innvilget

  @wip
  Scenario: Innvilget vedtak fra 14.12.2022
    Gitt en ny søknadBehandletHendelse
      | fødselsnummer | behandlingId | utfall |
      | 12345678901   | 1            | true   |
    #Og alle inngangsvilkår er oppfylt med virkningsdato "14.12.2022" og fastsatt abreidstid er 8 timer
    #Og sats er 488, grunnlag er 100000 og stønadsperiode er 52
    #Og beslutter kvalitetssikrer
    Så skal bruker ha 1 vedtak
