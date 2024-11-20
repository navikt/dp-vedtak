#language: no
@dokumentasjon @regel-samordning
Egenskap: § 4-25.Samordning med reduserte ytelser fra folketrygden, eller redusert avtalefestet pensjon

  Scenariomal: Søker oppfyller kravet til samordning
    Gitt at søker har søkt om dagpenger og har redusert ytelse
    Og har <antall barn> barn
    Og søker har redusert sykepenger "<sykepenger>"
    Og dagsats for sykepenger er "<sykepengerDagsats>"
    Og søker har redusert pleiepenger "<pleiepenger>"
    Og søker har redusert omsorgspenger "<omsorgspenger>"
    Og søker har redusert opplæringspenger "<opplæringspenger>"
    Og søker har redusert uføre "<uføre>"
    Og søker har redusert foreldrepenger "<foreldrepenger>"
    Og søker har redusert svangerskapspenger "<svangerskapspenger>"
    Så skal søker få samordnet dagsats "<samordnet>"
    Og gitt at bruker har "<grunnlag>" i grunnlag
    Så skal at bruker ha "<samordnetsats>" i samordnet dagsats
    Og utfall etter samordning skal være "<utfall etter samordning>"
    Eksempler:
      | antall barn | sykepenger | pleiepenger | omsorgspenger | opplæringspenger | uføre | foreldrepenger | svangerskapspenger | samordnet | sykepengerDagsats | grunnlag  | samordnetsats | utfall etter samordning |
      | 0           | Ja         | Nei         | Nei           | Nei              | Nei   | Nei            | Nei                | Ja        | 500               | 416666    | 500           | Ja                      |
      | 0           | Ja         | Nei         | Nei           | Nei              | Nei   | Nei            | Nei                | Ja        | 1100              | 416666    | 0             | Nei                     |
      | 2           | Ja         | Nei         | Nei           | Nei              | Nei   | Nei            | Nei                | Ja        | 1100              | 416667    | 0             | Ja                      |
