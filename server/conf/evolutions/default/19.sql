
# --- !Ups

CREATE TYPE EmoteKind AS ENUM ('heart', 'laugh', 'cry', 'scowl');

ALTER TABLE dote ADD COLUMN emote_kind EmoteKind;

# --- !Downs

ALTER TABLE dote DROP COLUMN emote_kind;
DROP TYPE EmoteKind;
