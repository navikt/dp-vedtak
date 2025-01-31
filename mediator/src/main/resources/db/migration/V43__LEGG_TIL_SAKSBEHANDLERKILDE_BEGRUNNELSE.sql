ALTER TABLE kilde_saksbehandler
    ADD COLUMN begrunnelse TEXT NULL,
    ADD COLUMN oppdatert   TIMESTAMP WITH TIME ZONE;