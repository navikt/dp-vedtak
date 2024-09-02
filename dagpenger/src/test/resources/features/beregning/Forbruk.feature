#language: no
Egenskap: Forbruker dagpengeperiode i slutten av perioden

  @wip
  Scenario: Har ikke arbeidet og skal forbruke siste rest av perioden
    Gitt at mottaker har vedtak med
      | Opplysning               | verdi | fraOgMed   | tilOgMed |
      | Terskel                  | 0.5   |            |          |
      | Innvilget periode        | 52    | 01.01.2020 |          |
      | Gjenstående stønadsdager | 5     | 01.01.2020 |          |
      | Sats                     | 100   | 01.01.2020 |          |
      | FVA                      | 40    | 01.01.2020 |          |
      | Egenandel                | 0     | 01.01.2020 |          |
    Når meldekort for periode som begynner fra og med 06.01.2020 mottas med
      | Dag     | type         | verdi |
      | Mandag  | Arbeidstimer | 0     |
      | Tirsdag | Arbeidstimer | 0     |
      | Onsdag  | Arbeidstimer | 0     |
      | Torsdag | Arbeidstimer | 0     |
      | Fredag  | Arbeidstimer | 0     |
      | Lørdag  | Arbeidstimer | 0     |
      | Søndag  | Arbeidstimer | 0     |
      | Mandag  | Arbeidstimer | 0     |
      | Tirsdag | Arbeidstimer | 0     |
      | Onsdag  | Arbeidstimer | 0     |
      | Torsdag | Arbeidstimer | 0     |
      | Fredag  | Arbeidstimer | 0     |
      | Lørdag  | Arbeidstimer | 0     |
      | Søndag  | Arbeidstimer | 0     |
    Så skal kravet til tapt arbeidstid være oppfylt
    Og utbetales 500,0 kroner
    Og utbetales 100,0 kroner på dag 1
    Og det forbrukes 5 dager
    Og det gjenstår 0 dager

  @wip
  Scenario: Har arbeidet over terskel de første dagene og skal forbruke siste rest av perioden
    Gitt at mottaker har vedtak med
      | Opplysning               | verdi | fraOgMed   | tilOgMed |
      | Terskel                  | 0.5   |            |          |
      | Innvilget periode        | 52    | 01.01.2020 |          |
      | Gjenstående stønadsdager | 5     | 01.01.2020 |          |
      | Sats                     | 100   | 01.01.2020 |          |
      | FVA                      | 40    | 01.01.2020 |          |
      | Egenandel                | 0     | 01.01.2020 |          |
    Når meldekort for periode som begynner fra og med 06.01.2020 mottas med
      | Dag     | type         | verdi |
      | Mandag  | Arbeidstimer | 8     |
      | Tirsdag | Arbeidstimer | 8     |
      | Onsdag  | Arbeidstimer | 4     |
      | Torsdag | Arbeidstimer | 0     |
      | Fredag  | Arbeidstimer | 0     |
      | Lørdag  | Arbeidstimer | 0     |
      | Søndag  | Arbeidstimer | 0     |
      | Mandag  | Arbeidstimer | 0     |
      | Tirsdag | Arbeidstimer | 0     |
      | Onsdag  | Arbeidstimer | 0     |
      | Torsdag | Arbeidstimer | 0     |
      | Fredag  | Arbeidstimer | 0     |
      | Lørdag  | Arbeidstimer | 0     |
      | Søndag  | Arbeidstimer | 0     |
    Så skal kravet til tapt arbeidstid være oppfylt
    Og utbetales 250,0 kroner
    Og det forbrukes 5 dager
    Og det gjenstår 0 dager

  # Alternativer
  # Arena-alternativ: Sjekke terskel på hele perioden, utbetale 0, forbruke 5 dager
  # Alternativ 2: Sjekke terskel for kun gjenstående dager og hvis over, ta med 1 og 1 dag til den er under terskel
  #               - Gir flere forbruksdager
  # Alternativ 3: Samme som 2, hvis over terskel, flytte perioden med 1 og en 1 dag til under terskel
  # Alternativ 4: Samme som Arena, men ikke forbruke dager
  # Alternativ 5: Sjekke terksel for kun gjenstående dager, og hvis over ikke forbruke dager, vent på neste meldekort

