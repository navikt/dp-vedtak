#language: no
@dokumentasjon @regel-dagpengegrunnlag
Egenskap: § 4-11. Dagpengegrunnlag

  Bakgrunn:
    Gitt at inntekt for grunnlag er
      | Beløp  | Inntektsklasse | Periode |
      | 500000 | ARBEIDSINNTEKT | 2020-04 |
      | 500000 | ARBEIDSINNTEKT | 2020-05 |
      | 50000  | ARBEIDSINNTEKT | 2021-06 |
      | 50000  | ARBEIDSINNTEKT | 2021-07 |

  Scenario: Grunnlag for dagpenger
    Gitt at søknadsdato er "01.04.2022"
    Så beregnet grunnlag være "1100000" og "1100000"
    Og vi har ikke avkortet
    #Og beregningsregel er "siste36"
