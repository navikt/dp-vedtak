DROP VIEW opplysningstabell;

ALTER TABLE opplysningstype
    DROP COLUMN kan_være_flere;

CREATE OR REPLACE VIEW opplysningstabell AS
SELECT opplysninger_opplysning.opplysninger_id,
       opplysning.id,
       opplysning.status,
       opplysningstype.datatype,
       opplysningstype.id                                                                                            AS type_id,
       opplysningstype.navn                                                                                          AS type_navn,
       opplysning.gyldig_fom,
       opplysning.gyldig_tom,
       opplysning_verdi.verdi_heltall,
       opplysning_verdi.verdi_desimaltall,
       opplysning_verdi.verdi_dato,
       opplysning_verdi.verdi_boolsk,
       opplysning_verdi.verdi_string,
       opplysning_utledning.regel                                                                                    AS utledet_av,
       kilde.id                                                                                                      AS kilde_id,
       opplysning.opprettet,
       (SELECT ARRAY_AGG(oua.utledet_av) FROM opplysning_utledet_av oua WHERE oua.opplysning_id = opplysning.id)     AS utledet_av_id,
       (SELECT ARRAY_AGG(oea.erstattet_av) FROM opplysning_erstattet_av oea WHERE oea.opplysning_id = opplysning.id) AS erstattet_av_id,
       opplysning_verdi.verdi_jsonb,
       opplysningstype.tekst_id,
       (SELECT opplysning_id
        FROM opplysning_erstattet_av
        WHERE erstattet_av = opplysning.id)                                                                          AS erstatter_id
FROM opplysning
         LEFT JOIN
     opplysninger_opplysning ON opplysning.id = opplysninger_opplysning.opplysning_id
         LEFT JOIN
     opplysningstype ON opplysning.opplysningstype_id = opplysningstype.opplysningstype_id
         LEFT JOIN
     opplysning_verdi ON opplysning.id = opplysning_verdi.opplysning_id
         LEFT JOIN
     opplysning_utledning ON opplysning.id = opplysning_utledning.opplysning_id
         LEFT JOIN
     kilde ON kilde_id = kilde.id
         LEFT JOIN
     opplysning_erstattet_av ON opplysning.id = opplysning_erstattet_av.opplysning_id
WHERE opplysning.fjernet IS FALSE

ORDER BY opplysning.id;
