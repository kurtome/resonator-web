# --- !Ups

CREATE TYPE TagKind AS ENUM ('metadata_flag', 'podcast_creator', 'podcast_genre');

CREATE TABLE tag (
  id BIGSERIAL PRIMARY KEY,
  kind TagKind NOT NULL,
  label TEXT NOT NULL,
  db_created_time TIMESTAMP NOT NULL DEFAULT current_timestamp,
  db_updated_time TIMESTAMP NOT NULL
);
CREATE TRIGGER tag_dbcreateupdatetimestamp_trigger
BEFORE INSERT ON tag
FOR EACH ROW EXECUTE PROCEDURE set_dbcreateupdatestamp_column();
CREATE TRIGGER tag_dbupdatetimestamp_trigger
BEFORE UPDATE ON tag
FOR EACH ROW EXECUTE PROCEDURE set_dbupdatestamp_column();
CREATE UNIQUE INDEX tag_id_kind_label_uniq_index
  ON tag(id, kind, label);

CREATE TABLE dotable_tag (
  tag_id BIGINT REFERENCES tag,
  dotable_id BIGINT REFERENCES dotable,
  db_created_time TIMESTAMP NOT NULL DEFAULT current_timestamp,
  db_updated_time TIMESTAMP NOT NULL
);
CREATE TRIGGER dotable_tag_dbcreateupdatetimestamp_trigger
BEFORE INSERT ON dotable_tag
FOR EACH ROW EXECUTE PROCEDURE set_dbcreateupdatestamp_column();
CREATE TRIGGER dotable_tag_dbupdatetimestamp_trigger
BEFORE UPDATE ON dotable_tag
FOR EACH ROW EXECUTE PROCEDURE set_dbupdatestamp_column();
CREATE UNIQUE INDEX dotable_tag_id_uniq_index
  ON dotable_tag(tag_id, dotable_id);

INSERT INTO tag (kind, label) VALUES ('metadata_flag'::TagKind, 'popular');

# --- !Downs

DROP INDEX dotable_tag_id_uniq_index;
DROP TRIGGER dotable_tag_dbupdatetimestamp_trigger ON dotable_tag;
DROP TRIGGER dotable_tag_dbcreateupdatetimestamp_trigger ON dotable_tag;
DROP TABLE dotable_tag;

DROP INDEX tag_id_kind_label_uniq_index;
DROP TRIGGER tag_dbupdatetimestamp_trigger ON tag;
DROP TRIGGER tag_dbcreateupdatetimestamp_trigger ON tag;
DROP TABLE tag;

DROP TYPE TagKind;
