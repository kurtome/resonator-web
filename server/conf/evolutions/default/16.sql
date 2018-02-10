
# --- !Ups

CREATE TABLE follower (
  id BIGSERIAL PRIMARY KEY,
  follow_time TIMESTAMP NOT NULL,
  follower_id BIGINT NOT NULL REFERENCES person(id),
  followee_id BIGINT NOT NULL REFERENCES person(id),
  db_created_time TIMESTAMP NOT NULL DEFAULT current_timestamp,
  db_updated_time TIMESTAMP NOT NULL
);
CREATE UNIQUE INDEX follower_followee_index on follower(followee_id, follower_id);
CREATE INDEX follower_follower_index on follower(follower_id);
CREATE TRIGGER follower_dbcreateupdatetimestamp_trigger
  BEFORE INSERT ON follower
  FOR EACH ROW EXECUTE PROCEDURE set_dbcreateupdatestamp_column();
CREATE TRIGGER follower_dbupdatetimestamp_trigger
  BEFORE UPDATE ON follower
  FOR EACH ROW EXECUTE PROCEDURE set_dbupdatestamp_column();

# --- !Downs

DROP INDEX follower_followee_index;
DROP INDEX follower_follower_index;
DROP TRIGGER follower_dbcreateupdatetimestamp_trigger ON follower;
DROP TRIGGER follower_dbupdatetimestamp_trigger ON follower;
DROP TABLE follower;
