#language: no
@dokumentasjon @regel-dagpengegrunnlag
Egenskap: § 4-11. Dagpengegrunnlag

  Scenario: Inntekt i siste 36 måneder er høyest og over 6G
    Gitt at søknadsdato for dagpenger er 01.04.2022
    Gitt at inntekt for grunnlag er
      | Beløp  | Inntektsklasse | Periode |
      | 500000 | ARBEIDSINNTEKT | 2020-04 |
      | 500000 | ARBEIDSINNTEKT | 2020-05 |
      | 50000  | ARBEIDSINNTEKT | 2021-06 |
      | 50000  | ARBEIDSINNTEKT | 2021-07 |
    Så beregnet grunnlag være "246131.33333333333333333333"
    Og uavkortet "1157655.060139122727185"
    Og vi har avkortet
    #Og beregningsregel er "siste36"

  Scenario: Inntekt i siste 12 måneder er høyest og over 6G
    Gitt at søknadsdato for dagpenger er 01.04.2022
    Gitt at inntekt for grunnlag er
      | Beløp  | Inntektsklasse | Periode |
      | 50000  | ARBEIDSINNTEKT | 2020-04 |
      | 50000  | ARBEIDSINNTEKT | 2020-05 |
      | 500000 | ARBEIDSINNTEKT | 2021-06 |
      | 500000 | ARBEIDSINNTEKT | 2021-07 |
    Så beregnet grunnlag være "638394"
    Og uavkortet "1000000"
    Og vi har avkortet
    #Og beregningsregel er "siste36"

  Scenario: Inntekt i siste 12 måneder er høyest og ikke over 6G
    Gitt at søknadsdato for dagpenger er 01.04.2022
    Gitt at inntekt for grunnlag er
      | Beløp | Inntektsklasse | Periode |
      | 10000 | ARBEIDSINNTEKT | 2020-04 |
      | 10000 | ARBEIDSINNTEKT | 2020-05 |
      | 10000 | ARBEIDSINNTEKT | 2021-06 |
      | 10000 | ARBEIDSINNTEKT | 2021-07 |
    Så beregnet grunnlag være "20000"
    Og uavkortet "20000"
    Og vi har ikke avkortet
    #Og beregningsregel er "siste36"