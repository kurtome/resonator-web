
# --- !Ups

CREATE INDEX CONCURRENTLY dotable_db_updated_time_id_compound_index on dotable(db_updated_time, id);

# --- !Downs

DROP INDEX dotable_db_updated_time_id_compound_index;

