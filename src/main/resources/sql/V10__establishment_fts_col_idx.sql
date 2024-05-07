-- Adds support for quick full-text searches against the `establishment` table.
ALTER TABLE establishment
    ADD COLUMN fts tsvector GENERATED ALWAYS AS
        (to_tsvector('english', coalesce(name, '') || ' ' ||
                                 coalesce(city, '') || ' ' ||
                                 coalesce(address, '') || ' ' ||
                                 coalesce(zip, '') || ' ' ||
                                 coalesce(phone, '') || ' ' ||
                                 coalesce(state, ''))) STORED;

CREATE INDEX establishment_fts_index ON establishment USING GIN (fts);
