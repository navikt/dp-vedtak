CREATE TABLE melding
(
    id                  BIGSERIAL PRIMARY KEY,
    ident               TEXT                                   NOT NULL,
    melding_id          uuid                                   NOT NULL UNIQUE,
    melding_type        TEXT                                   NOT NULL,
    data                jsonb                                  NOT NULL,
    lest_dato           TIMESTAMP WITH TIME ZONE DEFAULT NOW() NOT NULL,
    behandlet_tidspunkt TIMESTAMP WITH TIME ZONE
);

CREATE TABLE opplysningstype
(
    opplysningstype_id BIGSERIAL PRIMARY KEY,
    id                 TEXT NOT NULL,
    navn               TEXT NOT NULL,
    datatype           TEXT NOT NULL,
    parent             BIGINT                   DEFAULT NULL REFERENCES opplysningstype (opplysningstype_id),
    opprettet          TIMESTAMP WITH TIME ZONE DEFAULT NOW(),
    CONSTRAINT f UNIQUE (id, navn, datatype)
);

CREATE TABLE opplysning
(
    id                 uuid PRIMARY KEY,
    status             TEXT                     NOT NULL,
    opplysningstype_id BIGINT                   NOT NULL REFERENCES opplysningstype (opplysningstype_id),
    gyldig_fom         DATE DEFAULT NULL,
    gyldig_tom         DATE DEFAULT NULL,
    opprettet          TIMESTAMP WITH TIME ZONE NOT NULL
);

CREATE TABLE opplysning_verdi
(
    opplysning_id     uuid PRIMARY KEY,
    datatype          TEXT,
    verdi_heltall     INT,
    verdi_desimaltall DECIMAL,
    verdi_dato        DATE,
    verdi_boolsk      BOOLEAN,
    verdi_string      TEXT,
    opprettet         TIMESTAMP WITH TIME ZONE DEFAULT NOW(),
    FOREIGN KEY (opplysning_id) REFERENCES opplysning (id)
);

CREATE TABLE opplysning_kilde
(
    id            uuid PRIMARY KEY,
    opplysning_id uuid REFERENCES opplysning (id),
    type          TEXT                     NOT NULL,
    opprettet     TIMESTAMP WITH TIME ZONE NOT NULL,
    registrert    TIMESTAMP WITH TIME ZONE NOT NULL
);
CREATE TABLE opplysning_kilde_system
(
    kilde_id   uuid PRIMARY KEY REFERENCES opplysning_kilde (id),
    melding_id uuid REFERENCES melding (melding_id)
);
CREATE TABLE opplysning_kilde_saksbehandler
(
    kilde_id uuid PRIMARY KEY REFERENCES opplysning_kilde (id),
    ident    TEXT NOT NULL
);

CREATE TABLE opplysninger
(
    opplysninger_id uuid PRIMARY KEY,
    opprettet       TIMESTAMP WITH TIME ZONE DEFAULT NOW()
);

CREATE TABLE opplysninger_opplysning
(
    opplysninger_id uuid REFERENCES opplysninger (opplysninger_id),
    opplysning_id   uuid REFERENCES opplysning (id),
    CONSTRAINT unik_kobling UNIQUE (opplysninger_id, opplysning_id)
);


CREATE TABLE IF NOT EXISTS behandling
(
    behandling_id uuid PRIMARY KEY,
    tilstand      TEXT NOT NULL,
    opprettet     TIMESTAMP WITH TIME ZONE DEFAULT NOW()
);

CREATE TABLE IF NOT EXISTS behandler_hendelse
(
    melding_id    uuid PRIMARY KEY,             -- todo: REFERENCES melding (melding_id)
    ident         TEXT                     NOT NULL,
    ekstern_id    TEXT                     NOT NULL,
    hendelse_type TEXT                     NOT NULL,
    skjedde       TIMESTAMP WITH TIME ZONE NOT NULL,
    fagsak_id     TEXT                     NULL -- todo: Slett denne i framtida
);

CREATE TABLE IF NOT EXISTS behandler_hendelse_behandling
(
    behandling_id uuid NOT NULL REFERENCES behandling (behandling_id),
    melding_id    uuid NOT NULL REFERENCES behandler_hendelse (melding_id),
    CONSTRAINT behandler_hendelse_behandling_unik_kobling UNIQUE (behandling_id, melding_id)
);

CREATE TABLE IF NOT EXISTS behandling_opplysninger
(
    behandling_id   uuid NOT NULL REFERENCES behandling (behandling_id),
    opplysninger_id uuid NOT NULL REFERENCES opplysninger (opplysninger_id),
    CONSTRAINT behandling_opplysninger_unik_kobling UNIQUE (behandling_id, opplysninger_id)
);

CREATE TABLE IF NOT EXISTS behandling_basertpå
(
    behandling_id           uuid NOT NULL REFERENCES behandling (behandling_id),
    basert_på_behandling_id uuid NOT NULL REFERENCES behandling (behandling_id),
    CONSTRAINT behandling_basertpå_unik_kobling UNIQUE (behandling_id, basert_på_behandling_id)
);

CREATE TABLE IF NOT EXISTS person
(
    id        BIGSERIAL PRIMARY KEY,
    ident     TEXT NOT NULL UNIQUE,
    opprettet TIMESTAMP WITH TIME ZONE DEFAULT NOW()
);

CREATE TABLE IF NOT EXISTS person_behandling
(
    ident         TEXT NOT NULL REFERENCES person (ident),
    behandling_id uuid NOT NULL REFERENCES behandling (behandling_id),
    CONSTRAINT person_behandling_unik_kobling UNIQUE (ident, behandling_id)
);



CREATE TABLE IF NOT EXISTS opplysning_utledning
(
    opplysning_id uuid PRIMARY KEY,
    regel         TEXT,
    opprettet     TIMESTAMP WITH TIME ZONE DEFAULT NOW(),
    FOREIGN KEY (opplysning_id) REFERENCES opplysning (id)
);
CREATE TABLE IF NOT EXISTS opplysning_utledet_av
(
    opplysning_id uuid REFERENCES opplysning_utledning (opplysning_id),
    utledet_av    uuid REFERENCES opplysning (id),
    opprettet     TIMESTAMP WITH TIME ZONE DEFAULT NOW(),
    UNIQUE (opplysning_id, utledet_av)
);