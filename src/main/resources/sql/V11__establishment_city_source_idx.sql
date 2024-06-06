-- Adds support for quick full-text searches against the `establishment` table.
CREATE INDEX establishment_city_index
    ON establishment (city);

CREATE INDEX establishment_source_index
    ON establishment (source);
