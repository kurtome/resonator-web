
# --- !Ups

CREATE TABLE person (
  id    BIGSERIAL PRIMARY KEY,
  username TEXT NOT NULL,
  email TEXT NOT NULL,
  login_code TEXT,
  login_code_expiration_time TIMESTAMP NOT NULL DEFAULT current_timestamp,
  db_created_time TIMESTAMP NOT NULL DEFAULT current_timestamp,
  db_updated_time TIMESTAMP NOT NULL,
  CONSTRAINT username_length CHECK (LENGTH(username) > 3 AND LENGTH(username) <= 20),
  CONSTRAINT username_uniq UNIQUE(username),
  CONSTRAINT email_length CHECK (LENGTH(email) > 3 AND LENGTH(email) <= 254),
  CONSTRAINT email_uniq UNIQUE(email)
);
CREATE TRIGGER person_dbcreateupdatetimestamp_trigger
  BEFORE INSERT ON person
  FOR EACH ROW EXECUTE PROCEDURE set_dbcreateupdatestamp_column();
CREATE TRIGGER person_dbupdatetimestamp_trigger
  BEFORE UPDATE ON person
  FOR EACH ROW EXECUTE PROCEDURE set_dbupdatestamp_column();

CREATE TABLE auth_token (
  id    BIGSERIAL PRIMARY KEY,
  selector TEXT NOT NULL UNIQUE,
  validator BYTEA NOT NULL,
  expiration_time TIMESTAMP NOT NULL DEFAULT current_timestamp,
  person_id BIGINT NOT NULL REFERENCES person(id),
  db_created_time TIMESTAMP NOT NULL DEFAULT current_timestamp,
  db_updated_time TIMESTAMP NOT NULL
);
CREATE INDEX auth_token_selector_index ON auth_token(selector);
CREATE TRIGGER auth_token_dbcreateupdatetimestamp_trigger
  BEFORE INSERT ON auth_token
  FOR EACH ROW EXECUTE PROCEDURE set_dbcreateupdatestamp_column();
CREATE TRIGGER auth_token_dbupdatetimestamp_trigger
  BEFORE UPDATE ON auth_token
  FOR EACH ROW EXECUTE PROCEDURE set_dbupdatestamp_column();

# --- !Downs

DROP INDEX auth_token_selector_index;
DROP TRIGGER auth_token_dbcreateupdatetimestamp_trigger ON auth_token;
DROP TRIGGER auth_token_dbupdatetimestamp_trigger ON auth_token;
DROP TABLE auth_token;
DROP TRIGGER person_dbcreateupdatetimestamp_trigger ON person;
DROP TRIGGER person_dbupdatetimestamp_trigger ON person;
DROP TABLE person;
