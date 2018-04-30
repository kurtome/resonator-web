
# --- !Ups

ALTER TABLE search_index_queue ADD COLUMN last_batch_max_id BIGINT NOT NULL DEFAULT 0;

# --- !Downs

ALTER TABLE search_index_queue DROP COLUMN last_batch_max_id;
