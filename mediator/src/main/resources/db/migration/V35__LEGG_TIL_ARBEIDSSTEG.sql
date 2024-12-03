CREATE TABLE behandling_arbeidssteg
(
    behandling_id uuid                     NOT NULL REFERENCES behandling,
    oppgave       TEXT                     NOT NULL,
    tilstand      TEXT                     NOT NULL,
    utført_av     TEXT                     NULL,
    utført        TIMESTAMP WITH TIME ZONE NULL,
    CONSTRAINT behandling_arbeidssteg_unik_oppgave UNIQUE (behandling_id, oppgave)
)