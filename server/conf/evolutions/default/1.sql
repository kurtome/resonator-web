
# --- !Ups

CREATE TABLE dotable (
  id BIGSERIAL NOT NULL,
  title TEXT NULL,
  description TEXT NULL,
  PRIMARY KEY (id)
);

# --- !Downs
