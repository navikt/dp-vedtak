# dp-behandling

Behandler alle ulike hendelser på dagpenger.

## Implementasjonsmodell for behandling

- [Behandling og opplysninger](./opplysning/README.md)

## Avklaringer

I behandlingen kan det være behov for å avklare informasjon. Dette er en indikasjon på at opplysninger må sjekkes manuelt av en saksbehandler.

dp-behandling har en mekanisme for å lage avklaring ved å lage kontrollpunkter. 

Et kontrollpunkt er en indikasjon på at det er behov for å avklare informasjon. Feks. om en indikasjon på at søker har avtjent verneplikt.

```kotlin
val VernepliktKontroll =
        Kontrollpunkt(sjekker = Avklaringspunkter.Verneplikt) { opplysninger ->
            opplysninger.har(avtjentVerneplikt) && opplysninger.finnOpplysning(avtjentVerneplikt).verdi
        }
```

Kontrollpunktet kan brukes i en behandling for å avklare informasjon.

Hvis kontrollpunktet er sann vil det lages en Avklaring som legges til behandlingen.

## Flyt 

```mermaid
graph TD;
    Mottatt["Søknad mottatt"]
    Opprettet["Behandling opprettet"]
    ForslagFattet["Forslag fattet"]
    SkrevetTilArena["Vedtak skrevet til Arena"]

    Mottatt --> Behandling
    ForslagFattet --> VedtakKanSkrive

    ForslagTilVedtak["Forslag til vedtak"]
    BehandlingAvbrutt["Behandling avbrutt"]
    
    Redigert["Redigert"]
    Redigert --> Konklusjon

    Konklusjon{"Har konklusjon?"}
    Opprettet --> Konklusjon
    Konklusjon -->|Ja| Avklaring
    Konklusjon -.->|Endringer| Opprettet
    
    Avklaring{"Har avklaringer?"}
    Avklaring -->|Ja| ForslagTilVedtak
    Avklaring -->|Nei| KanFatteVedtak
    
    
    Forslag{"Forslag godkjent"}
    ForslagTilVedtak --> Forslag
    Forslag -->|Endringer| Redigert
    Forslag -->|Forkastes| BehandlingAvbrutt
    Forslag -->|Godkjennes| ForslagFattet
    
    VedtakKanSkrive{"Vedtak kan skrives"}
    VedtakKanSkrive -->|Nei| KunneIkkeSkrive
    VedtakKanSkrive -->|Ja| SkrevetTilArena
    
    KanFatteVedtak{"Kan fatte vedtak automatisk?"}
    KanFatteVedtak -->|Ja| VedtakKanSkrive
    KanFatteVedtak -->|Nei| Avbryt
    
    Avbryt["Avbryt behandling"]

    KunneIkkeSkrive["Kunne ikke skrives"]
    
    subgraph Behandling
        Opprettet
        Avklaring
        Forslag
        Redigert
        Konklusjon
        ForslagTilVedtak
        BehandlingAvbrutt
        ForslagFattet
        KanFatteVedtak
        Avbryt
    end
```

## Systemoversikt 

```mermaid
flowchart TD
    A[dp-soknad] 
    B[dp-behov-journalforing]
    C[dp-mottak]
    D[dp-behandling]
    E[dp-saksbehandling]
    F[dp-saksbehandling-frontend]
    G[Joark]
    H[dp-arena-sink]
    I[Arena]
    J[Behovløsere for opplysninger]
    K[dp-manuell-behandling]

    click A "https://github.com/navikt/dp-soknad" "dp-soknad"
    click B "https://github.com/navikt/dp-behov-journalforing" "dp-behov-journalforing"
    click C "https://github.com/navikt/dp-mottak" "dp-mottak"
    click D "https://github.com/navikt/dp-behandling" "dp-behandling"
    click E "https://github.com/navikt/dp-saksbehandling" "dp-saksbehandling"
    click F "https://github.com/navikt/dp-saksbehandling-frontend" "dp-saksbehandling-frontend"
    click H "https://github.com/navikt/dp-arena-sink" "dp-arena-sink"
    click J "https://github.com/navikt/dp-behandling/blob/main/docs/README.md#behov-for-opplysninger" "Behovløsere for opplysninger"


    A -->|Behov for å journalføre søknad|B
    B -->|Journalfører søknad|G
    G -->|Lytter på dagpenger journalføringer|C
    C -->|innsending_ferdigstilt|Behandling

subgraph Behandling
    D -->|Opplysningsbehov|J
    D -->|Manuell behandling avklaring?|K
    K -->|Manuell behandling avklart|D
    D -->|behandling_opprettet|E
    D -->|forslag_til_vedtak|E
    F -->|Oppgaver|E
    D -->|behandling_avbrutt|E
    F -->|Godkjenner/avbryt forslag til vedtak|D
    D -->|vedtak_fattet|Arena
end

subgraph Arena
    H -->|Skriver vedtak til|I
end
```


## Behov for opplysninger

[Komplett liste med behov](./behov.approved.md)

### Behov

Vi sender ut behov for opplysninger med denne konvolutten:

* **ident**: Fødselsnummer eller D-nummer
* **behandlingId**: ID på vår behandling. Legg denne på som logg kontekst 
* **søknadId**: ID på søknaden som behandlingen gjelder

```json
{
  "@id": "40a28aed-08ee-4744-9ba7-ada399e12e75",
  "@event_name": "behov",
  "@behovId": "018ee180-deef-7911-a91d-570b2d93a8b1",
  "@behov": [
    "opplysning-navn-1",
    "opplysning-navn-2"
  ],
  "ident": "12345678901",
  "søknadId" : "ed7d20ea-24e4-4b06-b796-a9bef6b3b012",
  "behandlingId": "018ee180-deef-7911-a91d-570b2d93a8b1",
  "@opplysningsbehov": true,
  "@opprettet": "2024-04-15T13:25:26.353152"
}
```

### Løsning

Behov kan løses enten som enkle verdier, eller med metadata:

Eksempel på enkel verdi:

```json
{
  /* ..resten av behovet */
  "@løsning": {
    "opplysning-navn-1": true,
    "opplysning-navn-2": 123123
  }
}
```

Eksempel på løsning med metadata:
```json
{
  /* ..resten av behovet */
  "@løsning": {
    "opplysning-navn-1": {
      "verdi": true,
      "gyldigFraOgMed": "2024-04-15",
      "gyldigTilOgMed": "2024-04-20"
    }
  }
}
```
