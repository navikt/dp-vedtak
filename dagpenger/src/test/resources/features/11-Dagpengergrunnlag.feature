#language: no
@dokumentasjon @regel-dagpengergrunnlag
Egenskap: § 4-11. Dagpengergrunnlag

  Bakgrunn:
    Gitt at søknadsdato er "01.04.2022"

  Scenariomal: Grunnlag for dagpenger
    Gitt at verneplikt for grunnlag er satt "<Verneplikt>"
    Og at inntekt for grunnlag er
      | Beløp | Inntektsklasse | Måned og år |
      | 50000 | ARBEIDSINNTEKT | 04.2021     |
      | 50000 | ARBEIDSINNTEKT | 05.2021     |
    Så beregnet utfall være "<avkortet grunnlag>" og "<uavkortet grunnlag>"

    Eksempler:
      | Verneplikt | avkortet grunnlag | uavkortet grunnlag |
      | Ja         | 100               |  100                  |