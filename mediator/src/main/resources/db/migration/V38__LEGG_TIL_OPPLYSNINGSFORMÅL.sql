-- Add the column with a default value
ALTER TABLE opplysningstype
    ADD COLUMN formål TEXT DEFAULT 'Regel';

-- Update existing rows to ensure they get the default value
UPDATE opplysningstype
SET formål = 'Regel'
WHERE formål IS NULL;

-- Remove the default value from the column
ALTER TABLE opplysningstype
    ALTER COLUMN formål DROP DEFAULT,
    ALTER COLUMN formål SET NOT NULL;
