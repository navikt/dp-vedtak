CREATE INDEX idx_opplysning_id ON opplysning (id);
CREATE INDEX idx_opplysning_opplysningstype_id ON opplysning (opplysningstype_id);
CREATE INDEX idx_opplysning_verdi_opplysning_id ON opplysning_verdi (opplysning_id);