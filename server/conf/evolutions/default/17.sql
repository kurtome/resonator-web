
# --- !Ups

ALTER TABLE podcast_feed_ingestion ADD COLUMN last_data_hash BYTEA NULL;
ALTER TABLE podcast_episode_ingestion ADD COLUMN last_data_hash BYTEA NULL;

# --- !Downs

ALTER TABLE podcast_feed_ingestion DROP COLUMN last_data_hash;
ALTER TABLE podcast_episode_ingestion DROP COLUMN last_data_hash;

