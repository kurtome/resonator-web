
# --- !Ups

CREATE TABLE dote (
  id BIGSERIAL PRIMARY KEY,
  dotable_id BIGINT REFERENCES dotable(id),
  person_id BIGINT REFERENCES person(id),
  smile_count INT DEFAULT 0,
  frown_count INT DEFAULT 0,
  cry_count INT DEFAULT 0,
  laugh_count INT DEFAULT 0,
  dote_time TIMESTAMP NOT NULL,
  db_created_time TIMESTAMP NOT NULL DEFAULT current_timestamp,
  db_updated_time TIMESTAMP NOT NULL
);
CREATE UNIQUE INDEX dote_dotable_person_index on dote(dotable_id, person_id);
CREATE INDEX dote_person_id_index on dote(person_id);
CREATE TRIGGER dote_dbcreateupdatetimestamp_trigger
  BEFORE INSERT ON dote
  FOR EACH ROW EXECUTE PROCEDURE set_dbcreateupdatestamp_column();
CREATE TRIGGER dote_dbupdatetimestamp_trigger
  BEFORE UPDATE ON dote
  FOR EACH ROW EXECUTE PROCEDURE set_dbupdatestamp_column();

# --- !Downs

DROP INDEX dote_dotable_person_index;
DROP INDEX dote_person_id_index;
DROP TRIGGER dote_dbcreateupdatetimestamp_trigger ON dote;
DROP TRIGGER dote_dbupdatetimestamp_trigger ON dote;
DROP TABLE dote;
