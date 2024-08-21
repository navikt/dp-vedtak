ALTER TABLE opplysning_kilde RENAME TO kilde;
ALTER TABLE opplysning_kilde_system RENAME TO kilde_system;
ALTER TABLE opplysning_kilde_saksbehandler RENAME TO kilde_saksbehandler;

ALTER TABLE opplysning
    ADD COLUMN kilde_id uuid NULL;

UPDATE opplysning SET kilde_id = (SELECT id FROM kilde WHERE opplysning.id = kilde.opplysning_id);

