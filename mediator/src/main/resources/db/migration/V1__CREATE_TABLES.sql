CREATE TABLE IF NOT EXISTS person
(
    id    BIGSERIAL PRIMARY KEY,
    ident VARCHAR(11) NOT NULL UNIQUE
);

CREATE TABLE IF NOT EXISTS vedtak
(
    id                UUID      NOT NULL PRIMARY KEY,
    person_id         BIGINT    NOT NULL REFERENCES person(id),
    behandling_id     UUID      NOT NULL, -- todo: I hvilken kontekst kommer behandling_id fra? dp-behandling eller dp-vedtak?
    virkningsdato     DATE      NOT NULL,
    vedtakstidspunkt  TIMESTAMP NOT NULL
);

CREATE INDEX IF NOT EXISTS vedtak_person_idx ON vedtak(person_id);