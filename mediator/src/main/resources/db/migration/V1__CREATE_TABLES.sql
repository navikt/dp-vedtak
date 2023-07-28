CREATE TABLE IF NOT EXISTS person
(
    id        BIGSERIAL PRIMARY KEY,
    ident     VARCHAR(11)                                                       NOT NULL UNIQUE,
    opprettet TIMESTAMP WITH TIME ZONE DEFAULT (NOW() AT TIME ZONE 'utc'::TEXT) NOT NULL
);


--- VEDTAK

CREATE TABLE IF NOT EXISTS vedtak
(
    id               UUID PRIMARY KEY,
    person_id        BIGINT                                                            NOT NULL REFERENCES person (id),
    behandling_id    UUID                                                              NOT NULL, -- todo: I hvilken kontekst kommer behandling_id fra? dp-behandling eller dp-vedtak?
    virkningsdato    DATE                                                              NOT NULL,
    vedtakstidspunkt TIMESTAMP                                                         NOT NULL,
    opprettet        TIMESTAMP WITH TIME ZONE DEFAULT (NOW() AT TIME ZONE 'utc'::TEXT) NOT NULL
);

CREATE INDEX IF NOT EXISTS vedtak_person_idx ON vedtak (person_id);

CREATE TABLE IF NOT EXISTS dagsats
(
    vedtak_id UUID PRIMARY KEY REFERENCES vedtak (id),
    beløp     DECIMAL NOT NULL
);


CREATE TABLE IF NOT EXISTS stønadsperiode
(
    vedtak_id    UUID PRIMARY KEY REFERENCES vedtak (id),
    antall_dager INTEGER NOT NULL
);


CREATE TABLE IF NOT EXISTS vanlig_arbeidstid
(
    vedtak_id            UUID PRIMARY KEY REFERENCES vedtak (id),
    antall_timer_per_dag DECIMAL NOT NULL
);

CREATE TABLE IF NOT EXISTS egenandel
(
    vedtak_id            UUID      PRIMARY KEY REFERENCES vedtak(id),
    beløp                DECIMAL   NOT NULL
);

CREATE TABLE IF NOT EXISTS rettighet
(
    id                   BIGSERIAL   PRIMARY KEY,
    vedtak_id            UUID        NOT NULL REFERENCES vedtak(id),
    rettighetstype       VARCHAR     NOT NULL,
    utfall               BOOLEAN     NOT NULL
);

CREATE INDEX IF NOT EXISTS rettighet_vedtak_idx ON rettighet(vedtak_id);

CREATE TABLE IF NOT EXISTS utbetaling
(
    vedtak_id           UUID PRIMARY KEY REFERENCES vedtak(id),
    utfall              BOOLEAN NOT NULL,
    forbruk             INTEGER NOT NULL,
    trukket_egenandel   DECIMAL NOT NULL
);

CREATE TABLE IF NOT EXISTS utbetalingsdag
(
    vedtak_id           UUID REFERENCES vedtak(id),
    dato                DATE NOT NULL,
    beløp               DECIMAL NOT NULL,
    PRIMARY KEY (vedtak_id, dato)
);


--- RAPPORTERING


CREATE TABLE IF NOT EXISTS rapporteringsperiode
(
    id        BIGSERIAL PRIMARY KEY,
    uuid      UUID                                                              NOT NULL UNIQUE,
    person_id BIGINT                                                            NOT NULL REFERENCES person (id),
    fom       DATE                                                              NOT NULL,
    tom       DATE                                                              NOT NULL,
    opprettet TIMESTAMP WITH TIME ZONE DEFAULT (NOW() AT TIME ZONE 'utc'::TEXT) NOT NULL,
    endret    TIMESTAMP                                                         NOT NULL
);




CREATE TABLE IF NOT EXISTS dag
(
    id                      BIGSERIAL PRIMARY KEY,
    rapporteringsperiode_id BIGINT NOT NULL REFERENCES rapporteringsperiode (id),
    dato                    DATE   NOT NULL,
    syk_timer               DECIMAL NULL,
    arbeid_timer            DECIMAL NUll,
    ferie_timer             DECIMAL NULL,
    opprettet               TIMESTAMP WITH TIME ZONE DEFAULT (NOW() AT TIME ZONE 'utc'::TEXT) NOT NULL,
    UNIQUE (rapporteringsperiode_id, dato)
);
--
-- CREATE TABLE IF NOT EXISTS aktivitet
-- (
--     id                      BIGSERIAL PRIMARY KEY,
--     person_id               BIGINT                                                            NOT NULL REFERENCES person (id),
--     rapporteringsperiode_id BIGINT                                                            NOT NULL REFERENCES rapporteringsperiode (id),
--     dato                    DATE                                                              NOT NULL,
--     "type"                  TEXT                                                              NOT NULL,
--     timer                   DECIMAL                                                           NOT NULL,
--     opprettet               TIMESTAMP WITH TIME ZONE DEFAULT (NOW() AT TIME ZONE 'utc'::TEXT) NOT NULL
-- );

