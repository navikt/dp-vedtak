CREATE TABLE opplysning
(
    id              uuid PRIMARY KEY,
    opplysningstype TEXT,
    fom             TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT '-infinity',
    tom             TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT 'infinity',
    opprettet       TIMESTAMP WITH TIME ZONE          DEFAULT NOW()
);

CREATE TABLE opplysning_kilde
(
    opplysning_id        uuid PRIMARY KEY,
    meldingsreferanse_id uuid,
    opprettet            TIMESTAMP WITH TIME ZONE DEFAULT NOW(),
    FOREIGN KEY (opplysning_id) REFERENCES opplysning (id)
);

CREATE TABLE opplysning_verdi
(
    opplysning_id     uuid PRIMARY KEY,
    verdi_heltall     INT,
    verdi_desimaltall DECIMAL,
    verdi_dato        TIMESTAMP WITH TIME ZONE,
    verdi_boolsk      BOOLEAN,
    verdi_ulid        TEXT,
    opprettet         TIMESTAMP WITH TIME ZONE DEFAULT NOW(),
    FOREIGN KEY (opplysning_id) REFERENCES opplysning (id)
);