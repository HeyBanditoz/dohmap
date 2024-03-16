ALTER TABLE establishment_location
ALTER COLUMN place_id DROP NOT NULL;

ALTER TABLE establishment_location
ALTER COLUMN raw_json DROP NOT NULL;
