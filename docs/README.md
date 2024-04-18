# dp-behandling

Behandler alle ulike hendelser på dagpenger.

## Implementasjonsmodell for behandling

- [Behandling og opplysninger](./opplysning/README.md)

## Flyt 

```mermaid
graph TD;
    A["Søknad mottatt"]
    B["Behandling opprettet"]
    D["Forslag fattet"]
    E["Vedtak skrevet til Arena"]

    A --> Behandling
    D --> X3

    C["Forslag til vedtak"]
    C1["Behandling avbrutt"]

    X1{"Må behandles 
      manuelt?"}
B --> X1
X1 -->|Ja| C1
X1 -->|Nei| C

X2{"Forslag godkjent"}
C --> X2
X2 -->|Forkastes| C1
X2 -->|Godkjennes| D
X2 -.->|Endringer| B

X3{"Vedtak kan
skrives"}
X3 -->|Nei| E1
X3 -->|Ja| E

E1["Kunne ikke skrives"]

subgraph Behandling
B
X1
X2
C
C1
D
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
