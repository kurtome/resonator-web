# --- !Ups

CREATE INDEX dotable_tag_dotable_id_index ON dotable_tag(dotable_id);

# --- !Downs

DROP INDEX dotable_tag_dotable_id_index;
