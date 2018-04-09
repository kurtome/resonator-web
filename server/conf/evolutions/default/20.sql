
# --- !Ups

ALTER TABLE dote DROP COLUMN smile_count;
ALTER TABLE dote DROP COLUMN laugh_count;
ALTER TABLE dote DROP COLUMN cry_count;
ALTER TABLE dote DROP COLUMN scowl_count;

# --- !Downs

ALTER TABLE dote ADD COLUMN smile_count INT NOT NULL DEFAULT 0;
ALTER TABLE dote ADD COLUMN laugh_count INT NOT NULL DEFAULT 0;
ALTER TABLE dote ADD COLUMN cry_count INT NOT NULL DEFAULT 0;
ALTER TABLE dote ADD COLUMN scowl_count INT NOT NULL DEFAULT 0;
