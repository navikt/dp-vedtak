#language: no
@dokumentasjon @regel-reell-arbeidssøker
Egenskap: § 4-5. Reelle arbeidssøkere

  Scenario: Søker fyller kravene til å være reell arbeidssøker
    Gitt at personen søker dagpenger
    Og kan jobbe både heltid og deltid
    Og kan jobbe i hele Norge
    Og kan ta alle typer arbeid
    Og er villig til å bytte yrke eller gå ned i lønn
    Så skal kravet til reell arbeidssøker være oppfylt

  Scenario: Søker fyller ikke kravene til å være reell arbeidssøker
    Gitt at personen søker dagpenger
    Og kan ikke jobbe både heltid og deltid
    Og kan ikke jobbe i hele Norge
    Og kan ikke ta alle typer arbeid
    Og er ikke villig til å bytte yrke eller gå ned i lønn
    Så skal kravet til reell arbeidssøker ikke være oppfylt

  Scenario: Søker fyller unntak til kravet til mobilitet og er reell arbeidssøker
    Gitt at personen søker dagpenger
    Og kan jobbe både heltid og deltid
    Og kan ikke jobbe i hele Norge
    Men oppfyller kravet til unntak for mobilitet
    Og kan ta alle typer arbeid
    Og er villig til å bytte yrke eller gå ned i lønn
    Så skal kravet til reell arbeidssøker være oppfylt
