ALTER TABLE establishment
ALTER COLUMN phone DROP NOT NULL;

UPDATE establishment
SET phone = NULL
WHERE phone ~ '^\s+';
