# § 4-19 Verneplikt (fastsetting)

## Regeltre

```mermaid
graph RL
  A["Faktor"] -->|"Oppslag"| B["Søknadstidspunkt"]
  C["Grunnlag for verneplikt"] -->|"Multiplikasjon"| D["Grunnbeløp for grunnlag"]
  C["Grunnlag for verneplikt"] -->|"Multiplikasjon"| A["Faktor"]
  E["Vernepliktperiode"] -->|"Oppslag"| B["Søknadstidspunkt"]
```

## Akseptansetester

```gherkin
#language: no
@dokumentasjon @regel-verneplikt-fastsetting
Egenskap: § 4-19 Verneplikt (fastsetting)

  Scenario: Gitt at søker oppfyller kravet verneplikt
    Gitt at søker har søkt om dagpenger under verneplikt 19.08.2024
    Så skal grunnlag være 372084
    Og dagpengerperioden være 26 uker
``` 