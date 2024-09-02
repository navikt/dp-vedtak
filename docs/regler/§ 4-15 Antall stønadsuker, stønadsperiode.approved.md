# § 4-15 Antall stønadsuker, stønadsperiode

## Regeltre

```mermaid
graph RL
  A["Kort dagpengeperiode"] -->|"Oppslag"| B["Søknadstidspunkt"]
  C["Lang dagpengeperiode"] -->|"Oppslag"| B["Søknadstidspunkt"]
  D["Terskelfaktor for 12 måneder"] -->|"Oppslag"| B["Søknadstidspunkt"]
  E["Terskelfaktor for 36 måneder"] -->|"Oppslag"| B["Søknadstidspunkt"]
  F["Divisior"] -->|"Oppslag"| B["Søknadstidspunkt"]
  G["Terskel for 12 måneder"] -->|"Multiplikasjon"| H["Grunnbeløp"]
  G["Terskel for 12 måneder"] -->|"Multiplikasjon"| D["Terskelfaktor for 12 måneder"]
  I["Terskel for 36 måneder"] -->|"Multiplikasjon"| H["Grunnbeløp"]
  I["Terskel for 36 måneder"] -->|"Multiplikasjon"| E["Terskelfaktor for 36 måneder"]
  J["Snittinntekt siste 36 måneder"] -->|"Divisjon"| K["Arbeidsinntekt siste 36 mnd"]
  J["Snittinntekt siste 36 måneder"] -->|"Divisjon"| F["Divisior"]
  L["Over terskel for 12 måneder"] -->|"StørreEnnEllerLik"| M["Arbeidsinntekt siste 12 mnd"]
  L["Over terskel for 12 måneder"] -->|"StørreEnnEllerLik"| G["Terskel for 12 måneder"]
  N["Over terskel for 36 måneder"] -->|"StørreEnnEllerLik"| J["Snittinntekt siste 36 måneder"]
  N["Over terskel for 36 måneder"] -->|"StørreEnnEllerLik"| I["Terskel for 36 måneder"]
  O["Stønadsuker ved siste 12 måneder"] -->|"HvisSannMedResultat"| L["Over terskel for 12 måneder"]
  O["Stønadsuker ved siste 12 måneder"] -->|"HvisSannMedResultat"| C["Lang dagpengeperiode"]
  O["Stønadsuker ved siste 12 måneder"] -->|"HvisSannMedResultat"| A["Kort dagpengeperiode"]
  P["Stønadsuker ved siste 36 måneder"] -->|"HvisSannMedResultat"| N["Over terskel for 36 måneder"]
  P["Stønadsuker ved siste 36 måneder"] -->|"HvisSannMedResultat"| C["Lang dagpengeperiode"]
  P["Stønadsuker ved siste 36 måneder"] -->|"HvisSannMedResultat"| A["Kort dagpengeperiode"]
  Q["Antall stønadsuker"] -->|"HøyesteAv"| O["Stønadsuker ved siste 12 måneder"]
  Q["Antall stønadsuker"] -->|"HøyesteAv"| P["Stønadsuker ved siste 36 måneder"]
  R["Antall dager i en arbeidsuke"] -->|"Oppslag"| B["Søknadstidspunkt"]
  S["Antall gjenstående stønadsdager"] -->|"Multiplikasjon"| Q["Antall stønadsuker"]
  S["Antall gjenstående stønadsdager"] -->|"Multiplikasjon"| R["Antall dager i en arbeidsuke"]
```

## Akseptansetester

```gherkin
#language: no
@dokumentasjon @regel-dapengeperiode
Egenskap: § 4-15 Antall stønadsuker, stønadsperiode

  Scenariomal: Søker har rett til dagpenger
    Gitt at søker har har rett til dagpenger fra "<virkningstidspunkt>"
    Og at søker har "<inntekt siste 12>" siste 12 måneder
    Og at søker har "<inntekt siste 36>" siste 36 måneder
    Så skal søker ha <antall uker> uker med dagpenger
    Eksempler:
      | virkningstidspunkt | inntekt siste 12 | inntekt siste 36 |  antall uker |
      | 01.08.2024         | 300000           | 0                |  104         |
      | 01.08.2024         | 200000           | 0                |  52          |
      | 01.08.2024         | 0                | 300000           |  52          |
      | 01.08.2024         | 0                | 200000           |  52          |
      | 01.08.2024         | 0                | 748000           |  104         |
      | 01.08.2024         | 300000           | 200000           |  104         |
      | 01.08.2024         | 248056           | 0                |  104         |
      | 01.08.2024         | 248055           | 0                |  52          |
      | 01.08.2024         | 248055           | 0                |  52          |
      | 01.08.2024         | 248056           | 0                |  104         |
``` 