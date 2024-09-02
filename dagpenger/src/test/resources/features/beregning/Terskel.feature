#language: no
Egenskap: Beregning av meldekort

  Scenario: Jobbet over terskel og får ingen utbetaling
    Gitt at mottaker har vedtak med
      | Opplysning               | verdi | fraOgMed   | tilOgMed |
      | Terskel                  | 0.5   |            |          |
      | Innvilget periode        | 52    | 01.01.2020 |          |
      | Gjenstående stønadsdager | 260   | 01.01.2020 |          |
      | Sats                     | 550   | 01.01.2020 |          |
      | FVA                      | 37.5  | 01.01.2020 |          |
      | Sats                     | 5555  | 13.01.2020 |          |
      | Egenandel                | 0     | 01.01.2020 |          |
    Når meldekort for periode som begynner fra og med 06.01.2020 mottas med
      | Dag     | type         | verdi |
      | Mandag  | Arbeidstimer | 5     |
      | Tirsdag | Arbeidstimer | 5     |
      | Onsdag  | Arbeidstimer | 5     |
      | Torsdag | Arbeidstimer | 5     |
      | Fredag  | Arbeidstimer | 5     |
      | Lørdag  |              |       |
      | Søndag  | Arbeidstimer | 2     |
      | Mandag  | Arbeidstimer | 4     |
      | Tirsdag | Arbeidstimer | 4     |
      | Onsdag  | Arbeidstimer | 4     |
      | Torsdag | Arbeidstimer | 4     |
      | Fredag  | Arbeidstimer | 4     |
      | Lørdag  |              | 0     |
      | Søndag  |              | 0     |
    Så skal kravet til tapt arbeidstid ikke være oppfylt

  Scenario: Jobbet nøyaktig på terskel og får 50% gradert utbetaling
    Gitt at mottaker har vedtak med
      | Opplysning               | verdi | fraOgMed   | tilOgMed |
      | Terskel                  | 0.5   |            |          |
      | Innvilget periode        | 52    | 01.01.2020 |          |
      | Gjenstående stønadsdager | 260   | 01.01.2020 |          |
      | Sats                     | 100   | 01.01.2020 |          |
      | FVA                      | 40    | 01.01.2020 |          |
      | Egenandel                | 0     | 01.01.2020 |          |
    Når meldekort for periode som begynner fra og med 06.01.2020 mottas med
      | Dag     | type         | verdi |
      | Mandag  | Arbeidstimer | 5     |
      | Tirsdag | Arbeidstimer | 5     |
      | Onsdag  | Arbeidstimer | 5     |
      | Torsdag | Arbeidstimer | 0     |
      | Fredag  | Arbeidstimer | 0     |
      | Lørdag  | Arbeidstimer | 0     |
      | Søndag  | Arbeidstimer | 5     |
      | Mandag  | Arbeidstimer | 0     |
      | Tirsdag | Arbeidstimer | 5     |
      | Onsdag  | Arbeidstimer | 5     |
      | Torsdag | Arbeidstimer | 5     |
      | Fredag  | Arbeidstimer | 5     |
      | Lørdag  | Arbeidstimer | 0     |
      | Søndag  | Arbeidstimer | 0     |
    Så skal kravet til tapt arbeidstid være oppfylt
    Og utbetales 500,0 kroner

  Scenario: Jobbet under terskel og får 25% gradert utbetaling
    Gitt at mottaker har vedtak med
      | Opplysning               | verdi | fraOgMed   | tilOgMed |
      | Terskel                  | 0.5   |            |          |
      | Innvilget periode        | 52    | 01.01.2020 |          |
      | Gjenstående stønadsdager | 260   | 01.01.2020 |          |
      | Sats                     | 101   | 01.01.2020 |          |
      | FVA                      | 37.5  | 01.01.2020 |          |
      | Egenandel                | 0     | 01.01.2020 |          |
    Når meldekort for periode som begynner fra og med 06.01.2020 mottas med
      | Dag     | type         | verdi |
      | Mandag  | Arbeidstimer | 5     |
      | Tirsdag | Arbeidstimer | 5     |
      | Onsdag  | Arbeidstimer | 5     |
      | Torsdag | Arbeidstimer | 4     |
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
    Og utbetales 754,1333333333331 kroner
    Og det forbrukes 10 dager

  Scenario: Jobbet under terskel og får 50% gradert utbetaling med endring av sats midt i perioden
    Gitt at mottaker har vedtak med
      | Opplysning               | verdi | fraOgMed   | tilOgMed   |
      | Terskel                  | 0.5   |            |            |
      | Innvilget periode        | 52    | 01.01.2020 |            |
      | Gjenstående stønadsdager | 260   | 01.01.2020 |            |
      | Sats                     | 100   | 01.01.2020 | 12.01.2020 |
      | FVA                      | 40    | 01.01.2020 |            |
      | Sats                     | 200   | 13.01.2020 |            |
      | Egenandel                | 0     | 01.01.2020 |            |
    Når meldekort for periode som begynner fra og med 06.01.2020 mottas med
      | Dag     | type         | verdi |
      | Mandag  | Arbeidstimer | 5     |
      | Tirsdag | Arbeidstimer | 5     |
      | Onsdag  | Arbeidstimer | 5     |
      | Torsdag | Arbeidstimer | 5     |
      | Fredag  | Arbeidstimer | 0     |
      | Lørdag  | Arbeidstimer | 0     |
      | Søndag  | Arbeidstimer | 0     |
      | Mandag  | Arbeidstimer | 5     |
      | Tirsdag | Arbeidstimer | 5     |
      | Onsdag  | Arbeidstimer | 5     |
      | Torsdag | Arbeidstimer | 5     |
      | Fredag  | Arbeidstimer | 0     |
      | Lørdag  | Arbeidstimer | 0     |
      | Søndag  | Arbeidstimer | 0     |
    Så skal kravet til tapt arbeidstid være oppfylt
    Og utbetales 750,0 kroner
    Og utbetales 50,0 kroner på dag 1
    Og utbetales 100,0 kroner på dag 10

  Scenario: Jobbet under terskel med sykdom og fravær og får 100% utbetaling for arbeidsdagene
    Gitt at mottaker har vedtak med
      | Opplysning               | verdi | fraOgMed   | tilOgMed |
      | Terskel                  | 0.5   |            |          |
      | Innvilget periode        | 52    | 01.01.2020 |          |
      | Gjenstående stønadsdager | 260   | 01.01.2020 |          |
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
      | Mandag  | Fravær       |       |
      | Tirsdag | Fravær       |       |
      | Onsdag  | Fravær       |       |
      | Torsdag | Fravær       |       |
      | Fredag  | Sykdom       |       |
      | Lørdag  | Arbeidstimer | 0     |
      | Søndag  | Arbeidstimer | 0     |
    Så skal kravet til tapt arbeidstid være oppfylt
    Og utbetales 500,0 kroner
    Og det forbrukes 5 dager
    Og det gjenstår 255 dager

  Scenario: Jobbet over terskel med sykdom og fravær og får ikke utbetaling
    Gitt at mottaker har vedtak med
      | Opplysning               | verdi | fraOgMed   | tilOgMed |
      | Terskel                  | 0.5   |            |          |
      | Innvilget periode        | 52    | 01.01.2020 |          |
      | Gjenstående stønadsdager | 260   | 01.01.2020 |          |
      | Sats                     | 100   | 01.01.2020 |          |
      | FVA                      | 40    | 01.01.2020 |          |
      | Egenandel                | 0     | 01.01.2020 |          |
    Når meldekort for periode som begynner fra og med 06.01.2020 mottas med
      | Dag     | type         | verdi |
      | Mandag  | Arbeidstimer | 5     |
      | Tirsdag | Arbeidstimer | 5     |
      | Onsdag  | Arbeidstimer | 5     |
      | Torsdag | Arbeidstimer | 6     |
      | Fredag  | Arbeidstimer | 0     |
      | Lørdag  | Arbeidstimer | 0     |
      | Søndag  | Arbeidstimer | 0     |
      | Mandag  | Fravær       |       |
      | Tirsdag | Fravær       |       |
      | Onsdag  | Fravær       |       |
      | Torsdag | Fravær       |       |
      | Fredag  | Sykdom       |       |
      | Lørdag  | Arbeidstimer | 0     |
      | Søndag  | Arbeidstimer | 0     |
    Så skal kravet til tapt arbeidstid ikke være oppfylt
    Og det forbrukes 0 dager

  Scenario: Endring av FVA midt i perioden
    Gitt at mottaker har vedtak med
      | Opplysning               | verdi | fraOgMed   | tilOgMed   |
      | Terskel                  | 0.5   |            |            |
      | Innvilget periode        | 52    | 01.01.2020 |            |
      | Gjenstående stønadsdager | 260   | 01.01.2020 |            |
      | Sats                     | 100   | 01.01.2020 |            |
      | FVA                      | 20    | 01.01.2020 | 12.01.2020 |
      | FVA                      | 40    | 13.01.2020 |            |
      | Egenandel                | 0     | 01.01.2020 |            |
    Når meldekort for periode som begynner fra og med 06.01.2020 mottas med
      | Dag     | type         | verdi |
      | Mandag  | Arbeidstimer | 5     |
      | Tirsdag | Arbeidstimer | 5     |
      | Onsdag  | Arbeidstimer | 5     |
      | Torsdag | Arbeidstimer | 5     |
      | Fredag  | Arbeidstimer | 5     |
      | Lørdag  | Arbeidstimer | 0     |
      | Søndag  | Arbeidstimer | 0     |
      | Mandag  | Arbeidstimer | 5     |
      | Tirsdag | Arbeidstimer | 0     |
      | Onsdag  | Arbeidstimer | 0     |
      | Torsdag | Arbeidstimer | 0     |
      | Fredag  | Arbeidstimer | 0     |
      | Lørdag  | Arbeidstimer | 0     |
      | Søndag  | Arbeidstimer | 0     |
    Så skal kravet til tapt arbeidstid være oppfylt
    Og utbetales 500,0 kroner

  Scenario: Endring av terskel midt i perioden, overgang fra permittering til ordinær
    Gitt at mottaker har vedtak med
      | Opplysning               | verdi | fraOgMed   | tilOgMed   |
      | Terskel                  | 0.6   |            | 12.01.2020 |
      | Terskel                  | 0.5   | 13.01.2020 |            |
      | Innvilget periode        | 52    | 01.01.2020 |            |
      | Gjenstående stønadsdager | 260   | 01.01.2020 |            |
      | Sats                     | 100   | 01.01.2020 |            |
      | FVA                      | 40    | 01.01.2020 |            |
      | Egenandel                | 0     | 01.01.2020 |            |
    Når meldekort for periode som begynner fra og med 06.01.2020 mottas med
      | Dag     | type         | verdi |
      | Mandag  | Arbeidstimer | 5     |
      | Tirsdag | Arbeidstimer | 5     |
      | Onsdag  | Arbeidstimer | 5     |
      | Torsdag | Arbeidstimer | 5     |
      | Fredag  | Arbeidstimer | 0     |
      | Lørdag  | Arbeidstimer | 0     |
      | Søndag  | Arbeidstimer | 0     |
      | Mandag  | Arbeidstimer | 5     |
      | Tirsdag | Arbeidstimer | 5     |
      | Onsdag  | Arbeidstimer | 5     |
      | Torsdag | Arbeidstimer | 5     |
      | Fredag  | Arbeidstimer | 2     |
      | Lørdag  | Arbeidstimer | 0     |
      | Søndag  | Arbeidstimer | 0     |
    Så skal kravet til tapt arbeidstid være oppfylt
    Og utbetales 475,0 kroner

  Scenario: Oppstart av vedtak midt i perioden
    Gitt at mottaker har vedtak med
      | Opplysning               | verdi | fraOgMed   | tilOgMed |
      | Terskel                  | 0.5   | 10.01.2020 |          |
      | Innvilget periode        | 52    | 10.01.2020 |          |
      | Gjenstående stønadsdager | 260   | 01.01.2020 |          |
      | Sats                     | 100   | 10.01.2020 |          |
      | FVA                      | 20    | 10.01.2020 |          |
      | Egenandel                | 0     | 01.01.2020 |          |
    Når meldekort for periode som begynner fra og med 06.01.2020 mottas med
      | Dag     | type         | verdi |
      | Mandag  | Arbeidstimer | 10    |
      | Tirsdag | Arbeidstimer | 10    |
      | Onsdag  | Arbeidstimer | 10    |
      | Torsdag | Arbeidstimer | 10    |
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
    Og utbetales 600,0 kroner
    Og det forbrukes 6 dager

