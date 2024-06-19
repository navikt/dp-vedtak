CREATE TABLE IF NOT EXISTS avklaringkode
(
    kode         TEXT PRIMARY KEY,
    tittel       TEXT    NOT NULL,
    beskrivelse  TEXT    NOT NULL,
    kankvitteres BOOLEAN NOT NULL,
    opprettet    TIMESTAMP WITH TIME ZONE DEFAULT NOW()

);

CREATE TABLE IF NOT EXISTS avklaring
(
    id             UUID PRIMARY KEY,
    avklaring_kode TEXT NOT NULL REFERENCES avklaringkode (kode),
    behandling_id  UUID NOT NULL REFERENCES behandling (behandling_id),
    opprettet      TIMESTAMP WITH TIME ZONE DEFAULT NOW()
);

CREATE TABLE IF NOT EXISTS avklaring_endring
(
    id            UUID PRIMARY KEY,
    avklaring_id  UUID      NOT NULL,
    endret        TIMESTAMP NOT NULL,
    type          TEXT      NOT NULL,
    saksbehandler TEXT,
    opprettet     TIMESTAMP WITH TIME ZONE DEFAULT NOW(),
    FOREIGN KEY (avklaring_id) REFERENCES avklaring (id)
);