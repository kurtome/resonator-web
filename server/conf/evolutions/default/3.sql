# --- !Ups

-- Recreate the index with just "kind" and "label" columns to enforce uniqueness within a kind.

DROP INDEX tag_id_kind_label_uniq_index;
CREATE UNIQUE INDEX tag_kind_label_uniq_index
  ON tag(kind, label);


# --- !Downs

DROP INDEX tag_id_kind_label_uniq_index;
CREATE UNIQUE INDEX tag_id_kind_label_uniq_index
  ON tag(id, kind, label);
