UPDATE opplysning
SET gyldig_fom = NULL,
    gyldig_tom = NULL
WHERE opplysning.id IN (SELECT opplysning.id
                        FROM opplysning
                        JOIN opplysningstype o ON o.opplysningstype_id = opplysning.opplysningstype_id
                        JOIN opplysninger_opplysning oo ON opplysning.id = oo.opplysning_id
                        JOIN behandling_opplysninger bo ON oo.opplysninger_id = bo.opplysninger_id
                        JOIN behandling b ON b.behandling_id = bo.behandling_id
                        WHERE b.tilstand NOT IN ('Ferdig', 'Avbrutt') AND o.id = 'søknadId');
