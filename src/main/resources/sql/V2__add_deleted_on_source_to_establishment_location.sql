CREATE TEMPORARY TABLE el_temp AS SELECT * FROM establishment_location;

DROP TABLE establishment_location;

CREATE TABLE establishment_location
(
    id               uuid             NOT NULL PRIMARY KEY,
    place_id         varchar          NOT NULL,
    establishment_id uuid REFERENCES establishment (id),
    lat              double precision NOT NULL,
    lng              double precision NOT NULL,
    source           smallint         NOT NULL,
    deleted_on       timestamp        NULL,
    raw_json         jsonb            NOT NULL
);

CREATE INDEX establishment_location_establishment_id_index
    ON establishment_location (establishment_id);

INSERT INTO establishment_location
    (id, place_id, establishment_id, lat, lng, source, deleted_on, raw_json)
    -- USING 0 for the `source` value as before the migration, only GOOGLE_MAPS_GEOCODING_API locations were stored
    (SELECT id, place_id, el_temp.establishment_id, lat, lng, 0, null, raw_json FROM el_temp);
