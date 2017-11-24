package kurtome.dote.server.db

import dote.proto.api.dotable.Dotable
import kurtome.dote.slick.db.TagKinds.TagKind

case class TagId(kind: TagKind, key: String)
case class Tag(id: TagId, name: String) {}
object Tag {
  def apply(kind: TagKind, key: String, name: String): Tag = Tag(TagId(kind, key), name)
}

case class TagList(tag: Tag, list: Seq[Dotable])
