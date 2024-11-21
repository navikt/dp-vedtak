# Dokumentasjon på behov for opplysninger

Dette er opplysninger som blir innhentet som en del av dagpengebehandlingen. De publiseres som behov på rapiden.

|Behov|Beskrivelse|Logisk datatype|Datatype|
|---|---|---|---|
|Er medlemmet ikke påvirket av streik eller lock-out? | Er medlemmet ikke påvirket av streik eller lock-out? | Boolsk|boolean|
|Fødselsdato | Fødselsdato | Dato|LocalDate|
|HelseTilAlleTyperJobb | Kan ta alle typer arbeid | Boolsk|boolean|
|InntektId | Inntekt | ULID|Ulid|
|InntektSiste12Mnd | Arbeidsinntekt siste 12 mnd | Penger|Beløp|
|InntektSiste36Mnd | Arbeidsinntekt siste 36 mnd | Penger|Beløp|
|KanJobbeDeltid | Kan jobbe heltid og deltid | Boolsk|boolean|
|KanJobbeHvorSomHelst | Kan jobbe i hele Norge | Boolsk|boolean|
|Krav til tap av arbeidsinntekt og arbeidstid | Krav til tap av arbeidsinntekt og arbeidstid | Boolsk|boolean|
|Lønnsgaranti | Har rett til dagpenger etter konkurs | Boolsk|boolean|
|Mottar ikke andre fulle ytelser | Mottar ikke andre fulle ytelser | Boolsk|boolean|
|Oppfyller krav til ikke utestengt | Oppfyller krav til ikke utestengt | Boolsk|boolean|
|Oppfyller personen vilkåret om medlemskap? | Oppfyller personen vilkåret om medlemskap? | Boolsk|boolean|
|Ordinær | Har rett til ordinære dagpenger gjennom arbeidsforhold | Boolsk|boolean|
|Permittert | Har rett til dagpenger under permittering | Boolsk|boolean|
|PermittertFiskeforedling | Har rett til dagpenger under permittering i fiskeforedlingsindustri | Boolsk|boolean|
|RegistrertSomArbeidssøker | Registrert som arbeidssøker | Boolsk|boolean|
|Søknadsdato | Søknadsdato | Dato|LocalDate|
|TarUtdanningEllerOpplæring | Tar utdanning eller opplæring? | Boolsk|boolean|
|Utfall etter samordning | Utfall etter samordning | Boolsk|boolean|
|Verneplikt | Avtjent verneplikt | Boolsk|boolean|
|VilligTilÅBytteYrke | Villig til å bytte yrke | Boolsk|boolean|
|søknadId | søknadId | Tekst|String|
|ØnskerDagpengerFraDato | Ønsker dagpenger fra dato | Dato|LocalDate|