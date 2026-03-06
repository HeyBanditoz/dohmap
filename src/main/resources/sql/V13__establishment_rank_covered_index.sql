DROP INDEX establishment_rank_establishment_id_index;

-- This covered index speeds up all queries that need granularity by one establishment ID.
-- Reverse id ordering is because it's a uuidv7, so to get latest we have to ORDER BY id DESC LIMIT 1
-- And grab rank too, to make it an index only scan (covered index)
CREATE UNIQUE INDEX establishment_rank_establishment_id_id_uindex
    ON establishment_rank (establishment_id ASC, id DESC, rank ASC);
