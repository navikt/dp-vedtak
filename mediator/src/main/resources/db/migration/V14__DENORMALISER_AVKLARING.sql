-- Flytt data fra avklaringkode til avklaring
ALTER TABLE avklaring
    ADD COLUMN kode          TEXT,
    ADD COLUMN tittel        TEXT,
    ADD COLUMN beskrivelse   TEXT,
    ADD COLUMN kan_kvitteres BOOLEAN;

-- Fyll inn data fra avklaringkode til avklaring
UPDATE avklaring
SET kode          = avklaringkode.kode,
    tittel        = avklaringkode.tittel,
    beskrivelse   = avklaringkode.beskrivelse,
    kan_kvitteres = avklaringkode.kan_kvitteres
FROM avklaringkode
WHERE avklaringkode.kode = avklaring.avklaring_kode;

-- Sett nye kolonner i avklaring til NOT NULL
ALTER TABLE avklaring
    ALTER COLUMN kode SET NOT NULL,
    ALTER COLUMN tittel SET NOT NULL,
    ALTER COLUMN beskrivelse SET NOT NULL,
    ALTER COLUMN kan_kvitteres SET NOT NULL;

-- Slett avklaringkode
ALTER TABLE avklaring
    DROP COLUMN avklaring_kode;
DROP TABLE avklaringkode;