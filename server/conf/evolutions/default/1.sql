# --- !Ups

CREATE OR REPLACE FUNCTION set_dbupdatestamp_column()
  RETURNS TRIGGER AS $$
BEGIN
  NEW.db_updated_time = now();;
  RETURN NEW;;
END;;
$$ language 'plpgsql';

CREATE OR REPLACE FUNCTION set_dbcreateupdatestamp_column()
  RETURNS TRIGGER AS $$
BEGIN
  NEW.db_created_time = now();;
  NEW.db_updated_time = now();;
RETURN NEW;;
END;;
$$ language 'plpgsql';

CREATE TYPE DotableKind AS ENUM ('podcast', 'podcast_episode');

CREATE TABLE dotable (
  id BIGSERIAL PRIMARY KEY,
  kind DotableKind NOT NULL,
  title TEXT NULL,
  description TEXT NULL,
  published_time TIMESTAMP NOT NULL,
  edited_time TIMESTAMP NOT NULL,
  parent_id BIGINT NULL REFERENCES dotable,
  common JSONB NOT NULL,
  details JSONB NOT NULL,
  db_created_time TIMESTAMP NOT NULL DEFAULT current_timestamp,
  db_updated_time TIMESTAMP NOT NULL
);
CREATE TRIGGER dotable_dbcreateupdatetimestamp_trigger
  BEFORE INSERT ON dotable
  FOR EACH ROW EXECUTE PROCEDURE set_dbcreateupdatestamp_column();
CREATE TRIGGER dotable_dbupdatetimestamp_trigger
  BEFORE UPDATE ON dotable
  FOR EACH ROW EXECUTE PROCEDURE set_dbupdatestamp_column();
CREATE INDEX dotable_kind_id_index ON dotable(kind, id);

-- Index for full text search on title
CREATE INDEX dotable_title_gin_idx ON dotable USING GIN (to_tsvector('english', title));

CREATE TABLE podcast_feed_ingestion (
  id BIGSERIAL PRIMARY KEY,
  feed_rss_url TEXT NOT NULL UNIQUE,
  itunes_id BIGINT NOT NULL UNIQUE,
  podcast_dotable_id BIGINT NOT NULL UNIQUE REFERENCES dotable,
  db_created_time TIMESTAMP NOT NULL DEFAULT current_timestamp,
  db_updated_time TIMESTAMP NOT NULL
);
CREATE TRIGGER podcast_feed_ingestion_dbcreateupdatetimestamp_trigger
  BEFORE INSERT ON podcast_feed_ingestion
  FOR EACH ROW EXECUTE PROCEDURE set_dbcreateupdatestamp_column();
CREATE TRIGGER podcast_feed_ingestion_dbupdatetimestamp_trigger
  BEFORE UPDATE ON podcast_feed_ingestion
  FOR EACH ROW EXECUTE PROCEDURE set_dbupdatestamp_column();

CREATE TABLE podcast_episode_ingestion (
  id BIGSERIAL PRIMARY KEY,
  podcast_dotable_id BIGINT NOT NULL REFERENCES dotable,
  guid TEXT NOT NULL,
  episode_dotable_id BIGINT NOT NULL UNIQUE REFERENCES dotable,
  db_created_time TIMESTAMP NOT NULL DEFAULT current_timestamp,
  db_updated_time TIMESTAMP NOT NULL
);
CREATE TRIGGER podcast_episode_ingestion_dbcreateupdatetimestamp_trigger
  BEFORE INSERT ON podcast_episode_ingestion
  FOR EACH ROW EXECUTE PROCEDURE set_dbcreateupdatestamp_column();
CREATE TRIGGER podcast_episode_ingestion_dbupdatetimestamp_trigger
  BEFORE UPDATE ON podcast_episode_ingestion
  FOR EACH ROW EXECUTE PROCEDURE set_dbupdatestamp_column();
CREATE UNIQUE INDEX podcast_episode_ingestion_podcast_episode_guid_uniq_index
  ON podcast_episode_ingestion(podcast_dotable_id, guid);



# --- !Downs

DROP INDEX podcast_episode_ingestion_podcast_episode_guid_uniq_index;
DROP TRIGGER podcast_episode_ingestion_dbcreateupdatetimestamp_trigger ON podcast_episode_ingestion;
DROP TRIGGER podcast_episode_ingestion_dbupdatetimestamp_trigger ON podcast_episode_ingestion;
DROP TABLE podcast_episode_ingestion;
DROP TRIGGER podcast_feed_ingestion_dbcreateupdatetimestamp_trigger ON podcast_feed_ingestion;
DROP TRIGGER podcast_feed_ingestion_dbupdatetimestamp_trigger ON podcast_feed_ingestion;
DROP TABLE podcast_feed_ingestion;
DROP INDEX dotable_title_gin_idx;
DROP INDEX dotable_kind_id_index;
DROP TRIGGER dotable_dbupdatetimestamp_trigger on dotable;
DROP TRIGGER dotable_dbcreateupdatetimestamp_trigger on dotable;
DROP TABLE dotable;
DROP TYPE DotableKind;
DROP FUNCTION set_dbupdatestamp_column();
DROP FUNCTION set_dbcreateupdatestamp_column();
