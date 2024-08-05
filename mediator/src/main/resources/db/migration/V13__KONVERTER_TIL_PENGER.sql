UPDATE opplysning_verdi og
SET verdi_string = 'NOK ' || ov.verdi_desimaltall::TEXT,
    --verdi_desimaltall = NULL,
    datatype     = 'Penger'
FROM opplysning_verdi ov
         LEFT JOIN opplysning o ON ov.opplysning_id = o.id
         LEFT JOIN opplysningstype ot ON o.opplysningstype_id = ot.opplysningstype_id
WHERE ov.opplysning_id = og.opplysning_id
  AND ot.id IN (
                'InntektSiste12Mnd',
                'InntektSiste36Mnd',
                'Grunnbeløp',
                'Inntektskrav for siste 12 mnd',
                'Inntektskrav for siste 36 mnd'
    )
  AND ov.datatype = 'Desimaltall'