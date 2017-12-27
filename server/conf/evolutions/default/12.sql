
# --- !Ups

ALTER TABLE dotable ADD COLUMN content_edited_time TIMESTAMP NOT NULL DEFAULT '01-01-1970'::TIMESTAMP;
CREATE INDEX dotable_content_edited_time_index ON dotable(content_edited_time);

# --- !Downs

DROP INDEX dotable_content_edited_time_index;
ALTER TABLE dotable DROP COLUMN content_edited_time;

