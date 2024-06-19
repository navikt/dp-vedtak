CREATE TABLE IF NOT EXISTS avklaringkode
(
    kode          TEXT PRIMARY KEY,
    tittel        TEXT    NOT NULL,
    beskrivelse   TEXT    NOT NULL,
    kan_kvitteres BOOLEAN NOT NULL,
    opprettet     TIMESTAMP WITH TIME ZONE DEFAULT NOW()

);

CREATE TABLE IF NOT EXISTS avklaring
(
    id             uuid PRIMARY KEY,
    avklaring_kode TEXT NOT NULL REFERENCES avklaringkode (kode),
    behandling_id  uuid NOT NULL REFERENCES behandling (behandling_id),
    opprettet      TIMESTAMP WITH TIME ZONE DEFAULT NOW(),
    UNIQUE (behandling_id, avklaring_kode)
);

CREATE INDEX IF NOT EXISTS avklaring_avklaring_kode_idx ON avklaring (avklaring_kode);
CREATE INDEX IF NOT EXISTS avklaring_behandling_id_idx ON avklaring (behandling_id);

CREATE TABLE IF NOT EXISTS avklaring_endring
(
    endring_id    uuid PRIMARY KEY,
    avklaring_id  uuid      NOT NULL REFERENCES avklaring (id),
    endret        TIMESTAMP NOT NULL,
    type          TEXT      NOT NULL,
    saksbehandler TEXT,
    opprettet     TIMESTAMP WITH TIME ZONE DEFAULT NOW()
);

CREATE INDEX IF NOT EXISTS avklaring_endring_avklaring_id_idx ON avklaring_endring (avklaring_id);