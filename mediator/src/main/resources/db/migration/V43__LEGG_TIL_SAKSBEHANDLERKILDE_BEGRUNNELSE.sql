ALTER TABLE kilde_saksbehandler
    ADD COLUMN begrunnelse TEXT NULL,
    ADD COLUMN begrunnelse_sist_endret TIMESTAMP WITH TIME ZONE;