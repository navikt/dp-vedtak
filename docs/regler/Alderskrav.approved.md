## Alderskrav - §4-23 Bortfall på grunn av alder
```mermaid
graph RL
  875007322["Aldersgrense"] -->|Oppslag| -1279085718["Virkningsdato"]
  1231931784["Dato søker når maks alder"] -->|LeggTilÅr| -320539334["Fødselsdato"]
  1231931784["Dato søker når maks alder"] -->|LeggTilÅr| 875007322["Aldersgrense"]
  1542970652["Siste mulige dag bruker kan oppfylle alderskrav"] -->|SisteDagIMåned| 1231931784["Dato søker når maks alder"]
  405013006["Oppfyller kravet til alder"] -->|FørEllerLik| -1279085718["Virkningsdato"]
  405013006["Oppfyller kravet til alder"] -->|FørEllerLik| 1542970652["Siste mulige dag bruker kan oppfylle alderskrav"]
  -1279085718["Virkningsdato"] -->|SisteAv| -1193640064["Søknadsdato"]
```