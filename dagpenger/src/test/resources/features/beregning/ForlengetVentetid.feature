#language: no
Egenskap: Forlenget ventetid sier vi må vente litt

  @wip
  Scenario: Har fått forlenget ventetid fordi hen har sagt opp selv
    Gitt at mottaker har vedtak med
      | Opplysning               | verdi | fraOgMed   | tilOgMed |
      | Terskel                  | 0.5   |            |          |
      | Innvilget periode        | 52    | 01.01.2020 |          |
      | Gjenstående stønadsdager | 5     | 01.01.2020 |          |
      #| Gjenstående ventetid     | 60    | 01.01.2020 |          |
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
    Og utbetales 0,0 kroner
    Og det forbrukes 10 dager
    Og det gjenstår 250 dager
    Og det gjenstår 50 ventedager
