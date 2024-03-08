CREATE TABLE melding
(
    id                  BIGSERIAL PRIMARY KEY,
    fnr                 TEXT                                   NOT NULL,
    melding_id          uuid                                   NOT NULL,
    melding_type        TEXT                                   NOT NULL,
    data                jsonb                                  NOT NULL,
    lest_dato           TIMESTAMP WITH TIME ZONE DEFAULT NOW() NOT NULL,
    behandlet_tidspunkt TIMESTAMP WITH TIME ZONE
);

CREATE TABLE opplysning
(
    id              uuid PRIMARY KEY,
    status          TEXT                        NOT NULL,
    opplysningstype TEXT                        NOT NULL,
    fom             TIMESTAMP WITHOUT TIME ZONE NOT NULL DEFAULT '-infinity',
    tom             TIMESTAMP WITHOUT TIME ZONE NOT NULL DEFAULT 'infinity',
    opprettet       TIMESTAMP WITH TIME ZONE             DEFAULT NOW()
);

CREATE TABLE opplysning_verdi
(
    opplysning_id     uuid PRIMARY KEY,
    datatype          TEXT,
    verdi_heltall     INT,
    verdi_desimaltall DECIMAL,
    verdi_dato        TIMESTAMP WITH TIME ZONE,
    verdi_boolsk      BOOLEAN,
    verdi_ulid        TEXT,
    opprettet         TIMESTAMP WITH TIME ZONE DEFAULT NOW(),
    FOREIGN KEY (opplysning_id) REFERENCES opplysning (id)
);

CREATE TABLE opplysning_kilde
(
    opplysning_id        uuid PRIMARY KEY,
    meldingsreferanse_id uuid,
    opprettet            TIMESTAMP WITH TIME ZONE DEFAULT NOW(),
    FOREIGN KEY (opplysning_id) REFERENCES opplysning (id)
    -- FOREIGN KEY (meldingsreferanse_id) REFERENCES melding (id)
);
