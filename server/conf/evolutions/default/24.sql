
# --- !Ups

CREATE INDEX CONCURRENTLY dotable_title_index on dotable(title);

# --- !Downs

DROP INDEX dotable_title_index;

