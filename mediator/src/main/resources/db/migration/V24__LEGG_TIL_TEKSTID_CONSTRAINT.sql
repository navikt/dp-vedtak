ALTER TABLE opplysningstype
    DROP CONSTRAINT f,
    ADD CONSTRAINT unik_opplysningstype UNIQUE (id, datatype);