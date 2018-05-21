
# --- !Ups

CREATE TYPE FrequencyKind AS ENUM ('AM', 'FM');

CREATE TABLE radio_station (
  id BIGSERIAL PRIMARY KEY,
  enabled BOOLEAN NOT NULL DEFAULT FALSE,
  frequency DECIMAL NOT NULL,
  frequency_kind FrequencyKind NOT NULL,
  call_sign CHAR(4) NOT NULL,
  db_created_time TIMESTAMP NOT NULL DEFAULT current_timestamp,
  db_updated_time TIMESTAMP NOT NULL
);
CREATE TRIGGER radio_station_dbcreateupdatetimestamp_trigger
  BEFORE INSERT ON radio_station
  FOR EACH ROW EXECUTE PROCEDURE set_dbcreateupdatestamp_column();
CREATE TRIGGER radio_station_dbupdatetimestamp_trigger
  BEFORE UPDATE ON radio_station
  FOR EACH ROW EXECUTE PROCEDURE set_dbupdatestamp_column();

CREATE UNIQUE INDEX radio_station_fequency_frequency_kind_uniq ON
  radio_station(frequency, frequency_kind);

CREATE UNIQUE INDEX radio_station_call_sign_uniq ON
  radio_station(call_sign);

CREATE TABLE radio_station_playlist (
  id BIGSERIAL PRIMARY KEY,
  station_id BIGINT NOT NULL REFERENCES radio_station,
  episode_id BIGINT NOT NULL REFERENCES dotable,
  start_time TIMESTAMP NOT NULL,
  end_time TIMESTAMP NOT NULL,
  db_created_time TIMESTAMP NOT NULL DEFAULT current_timestamp,
  db_updated_time TIMESTAMP NOT NULL
);
CREATE TRIGGER radio_station_playlist_dbcreateupdatetimestamp_trigger
  BEFORE INSERT ON radio_station_playlist
  FOR EACH ROW EXECUTE PROCEDURE set_dbcreateupdatestamp_column();
CREATE TRIGGER radio_station_playlist_dbupdatetimestamp_trigger
  BEFORE UPDATE ON radio_station_playlist
  FOR EACH ROW EXECUTE PROCEDURE set_dbupdatestamp_column();

CREATE UNIQUE INDEX radio_station_playlist_station_start_time_uniq ON radio_station_playlist(station_id, start_time);
CREATE INDEX radio_station_playlist_start_time_index ON radio_station_playlist(start_time);
CREATE INDEX radio_station_playlist_end_time_index ON radio_station_playlist(end_time);

CREATE TABLE radio_station_podcast (
  station_id BIGINT NOT NULL REFERENCES radio_station,
  podcast_id BIGINT NOT NULL REFERENCES dotable
);

CREATE UNIQUE INDEX radio_station_podcast_station_podcast_id_uniq_index
  ON radio_station_podcast(station_id, podcast_id);
CREATE INDEX radio_station_podcast_podcast_id_index ON radio_station_podcast(podcast_id);

# --- !Downs

DROP INDEX radio_station_podcast_station_podcast_id_uniq_index;
DROP INDEX radio_station_podcast_podcast_id_index;

DROP TABLE radio_station_podcast;

DROP INDEX radio_station_playlist_end_time_index;
DROP INDEX radio_station_playlist_start_time_index;
DROP INDEX radio_station_playlist_station_start_time_uniq;

DROP TRIGGER radio_station_playlist_dbupdatetimestamp_trigger ON radio_station_playlist;
DROP TRIGGER radio_station_playlist_dbcreateupdatetimestamp_trigger on radio_station_playlist;
DROP TABLE radio_station_playlist;

DROP TRIGGER radio_station_dbupdatetimestamp_trigger ON radio_station;
DROP TRIGGER radio_station_dbcreateupdatetimestamp_trigger ON radio_station;
DROP TABLE radio_station;

DROP TYPE FrequencyKind;
