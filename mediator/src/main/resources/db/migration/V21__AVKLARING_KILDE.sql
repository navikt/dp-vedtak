ALTER TABLE avklaring_endring
    DROP COLUMN saksbehandler,
    ADD COLUMN kilde_id uuid NULL REFERENCES kilde (id);