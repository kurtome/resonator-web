# --- !Ups

ALTER TABLE dotable_tag ALTER COLUMN dotable_id SET NOT NULL;
ALTER TABLE dotable_tag ALTER COLUMN tag_id SET NOT NULL;

# --- !Downs

ALTER TABLE dotable_tag ALTER COLUMN dotable_id DROP NOT NULL;
ALTER TABLE dotable_tag ALTER COLUMN tag_id DROP NOT NULL;
