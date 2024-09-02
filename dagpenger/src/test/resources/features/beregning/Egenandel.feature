#language: no
Egenskap: Beregning av meldekort

  Scenario: Skal få 50% gradert utbetaling, men trekkes for egenandel
    Gitt at mottaker har vedtak med
      | Opplysning               | verdi | fraOgMed   | tilOgMed |
      | Terskel                  | 0.5   |            |          |
      | Innvilget periode        | 52    | 01.01.2020 |          |
      | Gjenstående stønadsdager | 260   | 01.01.2020 |          |
      | Sats                     | 100   | 01.01.2020 |          |
      | FVA                      | 40    | 01.01.2020 |          |
      | Egenandel                | 300   | 01.01.2020 |          |
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
    Og det forbrukes 10 dager
    Og det forbrukes 300 i egenandel
    Og gjenstår 0 i egenandel
    Og utbetales 700,0 kroner

  Scenario: Oppstart midt i perioden, med 25% gradert utbetaling, men trekkes for egenandel
    Gitt at mottaker har vedtak med
      | Opplysning               | verdi | fraOgMed   | tilOgMed |
      | Terskel                  | 0.5   |            |          |
      | Innvilget periode        | 52    | 16.01.2020 |          |
      | Gjenstående stønadsdager | 260   | 01.01.2020 |          |
      | Sats                     | 100   | 01.01.2020 |          |
      | FVA                      | 40    | 01.01.2020 |          |
      | Egenandel                | 300   | 01.01.2020 |          |
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
      | Fredag  | Arbeidstimer | 4     |
      | Lørdag  | Arbeidstimer | 0     |
      | Søndag  | Arbeidstimer | 0     |
    Så skal kravet til tapt arbeidstid være oppfylt
    Og det forbrukes 2 dager
    Og det forbrukes 150 i egenandel
    Og gjenstår 150 i egenandel
    Og utbetales 0,0 kroner

  Scenario: Går fra høy til lav sats og forbruker all egenandel
    Gitt at mottaker har vedtak med
      | Opplysning               | verdi | fraOgMed   | tilOgMed   |
      | Terskel                  | 0.5   |            |            |
      | Innvilget periode        | 52    | 01.01.2020 |            |
      | Gjenstående stønadsdager | 260   | 01.01.2020 |            |
      | Sats                     | 1000  | 01.01.2020 | 12.01.2020 |
      | Sats                     | 100   | 13.01.2020 |            |
      | FVA                      | 40    | 01.01.2020 |            |
      | Egenandel                | 3000  | 01.01.2020 |            |
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
    Og det forbrukes 10 dager
    Og det forbrukes 3000 i egenandel
    Og gjenstår 0 i egenandel
    Og utbetales 2500,0 kroner
    Og utbetales 454,5454545454545 kroner på dag 1
    Og utbetales 45,45454545454546 kroner på dag 10

  Scenario: Har for lav sats til å få utbetaling og bruker ikke opp all egenandel
    Gitt at mottaker har vedtak med
      | Opplysning               | verdi | fraOgMed   | tilOgMed |
      | Terskel                  | 0.5   |            |          |
      | Innvilget periode        | 52    | 16.01.2020 |          |
      | Gjenstående stønadsdager | 260   | 01.01.2020 |          |
      | Sats                     | 1000  | 01.01.2020 |          |
      | FVA                      | 40    | 01.01.2020 |          |
      | Egenandel                | 3000  | 01.01.2020 |          |
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
    Og det forbrukes 2 dager
    Og det forbrukes 2000 i egenandel
    Og gjenstår 1000 i egenandel
    Og utbetales 0,0 kroner

  Scenario: Kan ikke bruke mer av egenandel enn gradert utbetaling
    Gitt at mottaker har vedtak med
      | Opplysning               | verdi | fraOgMed   | tilOgMed |
      | Terskel                  | 0.5   |            |          |
      | Innvilget periode        | 52    | 01.01.2020 |          |
      | Gjenstående stønadsdager | 260   | 01.01.2020 |          |
      | Sats                     | 500   | 01.01.2020 |          |
      | FVA                      | 40    | 01.01.2020 |          |
      | Egenandel                | 3000  | 01.01.2020 |          |
    Når meldekort for periode som begynner fra og med 06.01.2020 mottas med
      | Dag     | type         | verdi |
      | Mandag  | Arbeidstimer | 4     |
      | Tirsdag | Arbeidstimer | 4     |
      | Onsdag  | Arbeidstimer | 4     |
      | Torsdag | Arbeidstimer | 4     |
      | Fredag  | Arbeidstimer | 4     |
      | Lørdag  | Arbeidstimer | 0     |
      | Søndag  | Arbeidstimer | 0     |
      | Mandag  | Arbeidstimer | 4     |
      | Tirsdag | Arbeidstimer | 4     |
      | Onsdag  | Arbeidstimer | 4     |
      | Torsdag | Arbeidstimer | 4     |
      | Fredag  | Arbeidstimer | 4     |
      | Lørdag  | Arbeidstimer | 0     |
      | Søndag  | Arbeidstimer | 0     |
    Så skal kravet til tapt arbeidstid være oppfylt
    Og det forbrukes 10 dager
    Og det forbrukes 2500 i egenandel
    Og gjenstår 500 i egenandel
    Og utbetales 0,0 kroner

  Scenario: Eksempel 3 - Eksempel med satsendring, endringsvedtak.
    Gitt at mottaker har vedtak med
      | Opplysning               | verdi | fraOgMed   | tilOgMed   |
      | Terskel                  | 0.5   |            |            |
      | Innvilget periode        | 52    | 01.01.2020 |            |
      | Gjenstående stønadsdager | 260   | 01.01.2020 |            |
      | Sats                     | 800   | 01.01.2020 | 07.01.2020 |
      | Sats                     | 1200  | 08.01.2020 |            |
      | FVA                      | 40    | 01.01.2020 |            |
      | Egenandel                | 2400  | 01.01.2020 |            |
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
    Og det forbrukes 10 dager
    Og det forbrukes 2400 i egenandel
    Og gjenstår 0 i egenandel
    # TODO: Vi må håndtere avrunding
    Og utbetales 8800,000000000002 kroner
