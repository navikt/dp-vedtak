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


--- RAPPORTERING


CREATE TABLE IF NOT EXISTS rapporteringsperiode
(
    id        BIGSERIAL PRIMARY KEY,
    uuid      uuid                                                              NOT NULL UNIQUE,
    person_id BIGINT                                                            NOT NULL REFERENCES person (id),
    fom       DATE                                                              NOT NULL,
    tom       DATE                                                              NOT NULL,
    opprettet TIMESTAMP WITH TIME ZONE DEFAULT (NOW() AT TIME ZONE 'utc'::TEXT) NOT NULL,
    endret    TIMESTAMP                                                         NOT NULL
);


CREATE TABLE IF NOT EXISTS dag
(
    id                      BIGSERIAL PRIMARY KEY,
    person_id               BIGINT NOT NULL REFERENCES person (id), -- Blir dette litt smør på flesk, siden rapporteringsperiode refererer person?
    rapporteringsperiode_id BIGINT NOT NULL REFERENCES rapporteringsperiode (id),
    dato                    DATE   NOT NULL,
    UNIQUE (rapporteringsperiode_id, dato)
);

CREATE TABLE IF NOT EXISTS aktivitet
(
    id        BIGSERIAL PRIMARY KEY,
    person_id BIGINT                                                            NOT NULL REFERENCES person (id),
    dato      DATE                                                              NOT NULL,
    "type"    TEXT                                                              NOT NULL,
    timer     DECIMAL                                                           NOT NULL,
    opprettet TIMESTAMP WITH TIME ZONE DEFAULT (NOW() AT TIME ZONE 'utc'::TEXT) NOT NULL
);

