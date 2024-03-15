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
    gyldig_fom         TIMESTAMP WITH TIME ZONE NULL DEFAULT NULL,
    gyldig_tom         TIMESTAMP WITH TIME ZONE NULL DEFAULT NULL,
    opprettet          TIMESTAMP WITH TIME ZONE      DEFAULT NOW()
);

CREATE TABLE opplysning_verdi
(
    opplysning_id     uuid PRIMARY KEY,
    datatype          TEXT,
    verdi_heltall     INT,
    verdi_desimaltall DECIMAL,
    verdi_dato        TIMESTAMP WITH TIME ZONE,
    verdi_boolsk      BOOLEAN,
    verdi_string      TEXT,
    opprettet         TIMESTAMP WITH TIME ZONE DEFAULT NOW(),
    FOREIGN KEY (opplysning_id) REFERENCES opplysning (id)
);

CREATE TABLE opplysning_kilde
(
    opplysning_id        uuid PRIMARY KEY,
    meldingsreferanse_id uuid REFERENCES melding (melding_id),
    opprettet            TIMESTAMP WITH TIME ZONE DEFAULT NOW()
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
    opprettet     TIMESTAMP WITH TIME ZONE DEFAULT NOW()
);

CREATE TABLE IF NOT EXISTS behandler_hendelse
(
    melding_id    uuid PRIMARY KEY, -- todo: REFERENCES melding (melding_id)
    ident         TEXT                     NOT NULL,
    ekstern_id    TEXT                     NOT NULL,
    hendelse_type TEXT                     NOT NULL,
    skjedde       TIMESTAMP WITH TIME ZONE NOT NULL
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
    opprettet TIMESTAMP WITH TIME ZONE DEFAULT NOW()
);

CREATE TABLE IF NOT EXISTS person_identer
(
    person_id BIGINT NOT NULL REFERENCES person (id),
    ident     TEXT   NOT NULL UNIQUE,
    opprettet TIMESTAMP WITH TIME ZONE DEFAULT NOW()
);

CREATE VIEW person_view AS
SELECT person.id AS person_id, person.opprettet AS person_opprettet, person_identer.ident AS person_ident
FROM person
         JOIN person_identer ON person.id = person_identer.person_id;