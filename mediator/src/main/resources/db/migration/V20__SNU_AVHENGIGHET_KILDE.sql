
ALTER TABLE kilde DROP CONSTRAINT opplysning_kilde_opplysning_id_fkey;

ALTER TABLE kilde DROP COLUMN opplysning_id;

ALTER TABLE opplysning ADD CONSTRAINT kilde_id_fkey FOREIGN KEY (kilde_id) REFERENCES kilde (id);

