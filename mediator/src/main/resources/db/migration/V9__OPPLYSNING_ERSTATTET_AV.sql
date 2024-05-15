CREATE TABLE IF NOT EXISTS opplysning_erstattet_av
(
    opplysning_id uuid PRIMARY KEY REFERENCES opplysning (id),
    erstattet_av  uuid NOT NULL REFERENCES opplysning (id),
    opprettet     TIMESTAMP WITH TIME ZONE DEFAULT NOW()
);