-- Adds system identifiers (the unique identifier the data source gives this particular entity) which can be used a
-- solid option for matching.
ALTER TABLE establishment
    ADD COLUMN sys_id VARCHAR NULL;

CREATE INDEX establishment_sys_id_index
    ON establishment (sys_id);

COMMENT ON COLUMN establishment.sys_id IS $$This column uniquely identifies an establishment from a particular datasource, as long as it isn't null.$$;

ALTER TABLE inspection
    ADD COLUMN sys_id VARCHAR NULL;

CREATE INDEX inspection_sys_id_index
    ON inspection (sys_id);

COMMENT ON COLUMN inspection.sys_id IS $$This column uniquely identifies an inspection from a particular datasource, as long as it isn't null.$$;
