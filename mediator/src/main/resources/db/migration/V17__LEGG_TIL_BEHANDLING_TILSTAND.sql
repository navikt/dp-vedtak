CREATE TABLE behandling_tilstand
(
    id            BIGSERIAL PRIMARY KEY,
    behandling_id uuid                     NOT NULL REFERENCES behandling (behandling_id),
    tilstand      TEXT                     NOT NULL,
    endret        TIMESTAMP WITH TIME ZONE NOT NULL,
    UNIQUE (behandling_id, tilstand, endret)
);

INSERT INTO behandling_tilstand (behandling_id, tilstand, endret)
SELECT behandling_id, tilstand, sist_endret_tilstand
FROM behandling;