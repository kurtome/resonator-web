# --- !Ups

ALTER TABLE podcast_feed_ingestion ALTER COLUMN last_feed_etag DROP NOT NULL;
ALTER TABLE podcast_feed_ingestion ALTER COLUMN last_feed_etag DROP DEFAULT;
UPDATE podcast_feed_ingestion SET last_feed_etag = NULL WHERE last_feed_etag = '';

# --- !Downs

UPDATE podcast_feed_ingestion SET last_feed_etag = '' WHERE last_feed_etag IS NULL;
ALTER TABLE podcast_feed_ingestion ALTER COLUMN last_feed_etag SET DEFAULT '';
ALTER TABLE podcast_feed_ingestion ALTER COLUMN last_feed_etag SET NOT NULL;
