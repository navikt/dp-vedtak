CREATE INDEX idx_opplysning_fagsak_id
    ON opplysning_verdi (verdi_heltall)
    WHERE verdi_heltall IS NOT NULL