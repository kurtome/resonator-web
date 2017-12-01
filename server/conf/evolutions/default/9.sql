# --- !Ups

CREATE INDEX dotable_parent_id_index ON dotable(parent_id);

# --- !Downs

DROP INDEX dotable_parent_id_index;
