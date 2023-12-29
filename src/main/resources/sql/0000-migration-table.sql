CREATE TABLE IF NOT EXISTS databasechangelog
(
    filename varchar NOT NULL PRIMARY KEY,
    executed_by varchar NOT NULL,
    executed_when timestamptz NOT NULL DEFAULT now()
);