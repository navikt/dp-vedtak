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

CREATE VIEW opplysningstabell AS
SELECT opplysning.id,
       opplysning.status,
       opplysningstype.datatype,
       opplysningstype.id   AS type_id,
       opplysningstype.navn AS type_navn,
       opplysning.gyldig_fom,
       opplysning.gyldig_tom,
       opplysning_verdi.verdi_heltall,
       opplysning_verdi.verdi_desimaltall,
       opplysning_verdi.verdi_dato,
       opplysning_verdi.verdi_boolsk,
       opplysning_verdi.verdi_string
FROM opplysning
         LEFT JOIN
     opplysningstype ON opplysning.opplysningstype_id = opplysningstype.opplysningstype_id
         LEFT JOIN
     opplysning_verdi ON opplysning.id = opplysning_verdi.opplysning_id;


