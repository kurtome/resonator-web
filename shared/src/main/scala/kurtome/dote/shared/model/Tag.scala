package kurtome.dote.shared.model

import kurtome.dote.proto.api.dotable.Dotable
import kurtome.dote.shared.constants.TagKinds.TagKind

case class TagId(kind: TagKind, key: String)

case class Tag(id: TagId, name: String) {}

object Tag {
  def apply(kind: TagKind, key: String, name: String): Tag = Tag(TagId(kind, key), name)
}

case class TagList(tag: Tag, list: Seq[Dotable])
