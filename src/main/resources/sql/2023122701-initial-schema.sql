CREATE TABLE establishment
(
    id      uuid    NOT NULL PRIMARY KEY,
    name    varchar NOT NULL,
    address varchar NOT NULL,
    city    varchar NOT NULL,
    state   char(2) NOT NULL,
    zip     varchar NOT NULL,
    phone   varchar NOT NULL,
    type    varchar NOT NULL
);

CREATE INDEX establishment_type_index
    ON establishment (type);

CREATE TABLE establishment_rank
(
    id               uuid NOT NULL PRIMARY KEY,
    establishment_id uuid REFERENCES establishment (id),
    rank             int  NULL
);

CREATE INDEX establishment_rank_establishment_id_index
    ON establishment_rank (establishment_id DESC);

CREATE TABLE establishment_location
(
    id               uuid             NOT NULL PRIMARY KEY,
    place_id         varchar          NOT NULL,
    establishment_id uuid REFERENCES establishment (id),
    lat              double precision NOT NULL,
    lng              double precision NOT NULL,
    raw_json         jsonb            NOT NULL
);

CREATE INDEX establishment_location_establishment_id_index
    ON establishment_location (establishment_id);

CREATE TABLE inspection
(
    id               uuid    NOT NULL PRIMARY KEY,
    establishment_id uuid REFERENCES establishment (id),
    inspection_date  date    NOT NULL,
    inspection_type  varchar NULL,
    score            int     NULL
);

CREATE TABLE violation
(
    id                      uuid    NOT NULL PRIMARY KEY,
    inspection_id           uuid REFERENCES inspection (id),
    code                    varchar NULL,
    observed                varchar NULL,
    points                  int     NULL,
    critical                bool    NULL,
    occurrences             int     NULL,
    corrected_on_site       boolean NULL,
    public_health_rationale varchar NULL
);

CREATE INDEX violation_inspection_id_index
    ON violation (inspection_id);
