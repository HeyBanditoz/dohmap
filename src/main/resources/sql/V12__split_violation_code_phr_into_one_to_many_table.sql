CREATE TABLE violation_code_phr
(
    id                      SERIAL PRIMARY KEY,
    code                    TEXT,
    public_health_rationale TEXT
);

INSERT INTO violation_code_phr (code, public_health_rationale)
SELECT code, public_health_rationale
FROM violation v
GROUP BY code, public_health_rationale;

ALTER TABLE violation
    ADD COLUMN violation_code_phr_id INT REFERENCES violation_code_phr (id);

UPDATE violation v
SET violation_code_phr_id = (SELECT vcp.id
                             FROM violation_code_phr vcp
                             WHERE v.code IS NOT DISTINCT FROM vcp.code
                               AND v.public_health_rationale IS NOT DISTINCT FROM vcp.public_health_rationale);

ALTER TABLE violation
    ALTER COLUMN violation_code_phr_id SET NOT NULL;

ALTER TABLE violation
    DROP COLUMN code,
    DROP COLUMN public_health_rationale;

-- it is recommended to run the following query to free up space:
-- VACUUM FULL violation;
