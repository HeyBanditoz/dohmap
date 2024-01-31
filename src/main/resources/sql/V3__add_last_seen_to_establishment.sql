ALTER TABLE establishment
ADD COLUMN last_seen timestamp DEFAULT NOW() NOT NULL;