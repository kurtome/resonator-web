
# --- !Ups



CREATE TABLE search_index_queue (
  index_name TEXT NOT NULL,
  sync_completed_through_time TIMESTAMP NOT NULL DEFAULT '1970-01-01'::TIMESTAMP
);
INSERT INTO search_index_queue (index_name) VALUES ('dotables');

CREATE INDEX dotable_db_updated_time_index ON dotable(db_updated_time);

# --- !Downs

DROP INDEX dotable_db_updated_time_index;
DROP TABLE search_index_queue;

