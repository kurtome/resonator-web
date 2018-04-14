
# --- !Ups

ALTER TABLE dote ADD COLUMN half_stars INT NOT NULL DEFAULT 0;

# --- !Downs

ALTER TABLE dote DROP COLUMN half_stars;
