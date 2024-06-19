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

CREATE TABLE IF NOT EXISTS avklaring_endring
(
    id            uuid PRIMARY KEY,
    avklaring_id  uuid      NOT NULL,
    endret        TIMESTAMP NOT NULL,
    type          TEXT      NOT NULL,
    saksbehandler TEXT,
    opprettet     TIMESTAMP WITH TIME ZONE DEFAULT NOW(),
    FOREIGN KEY (avklaring_id) REFERENCES avklaring (id)
);