
# --- !Ups

ALTER TABLE podcast_feed_ingestion ADD COLUMN reingest_wait_minutes BIGINT NOT NULL DEFAULT 60;

# --- !Downs

ALTER TABLE podcast_feed_ingestion DROP COLUMN reingest_wait_minutes;

