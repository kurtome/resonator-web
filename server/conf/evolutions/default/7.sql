# --- !Ups

ALTER TABLE podcast_feed_ingestion ALTER COLUMN podcast_dotable_id DROP NOT NULL;
ALTER TABLE podcast_feed_ingestion ADD COLUMN next_ingestion_time TIMESTAMP NOT NULL DEFAULT now();
ALTER TABLE podcast_feed_ingestion ADD COLUMN last_feed_etag TEXT NOT NULL DEFAULT '';

# --- !Downs

DELETE FROM podcast_feed_ingestion WHERE podcast_dotable_id IS NULL;
ALTER TABLE podcast_feed_ingestion ALTER COLUMN podcast_dotable_id SET NOT NULL;
ALTER TABLE podcast_feed_ingestion DROP COLUMN next_ingestion_time;
ALTER TABLE podcast_feed_ingestion DROP COLUMN last_feed_etag;
