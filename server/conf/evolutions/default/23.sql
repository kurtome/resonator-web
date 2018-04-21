
# --- !Ups

ALTER TABLE dote ADD COLUMN review_dotable_id BIGINT REFERENCES dotable(id);
CREATE UNIQUE INDEX dote_review_dotable_id on dote(review_dotable_id);

# --- !Downs

DROP INDEX dote_review_dotable_id;
ALTER TABLE dote DROP COLUMN review_dotable_id;
