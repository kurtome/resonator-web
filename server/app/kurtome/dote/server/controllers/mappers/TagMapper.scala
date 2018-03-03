package kurtome.dote.server.controllers.mappers

import kurtome.dote.proto.api.tag.Tag
import kurtome.dote.server.model
import kurtome.dote.slick.db.TagKinds
import kurtome.dote.slick.db.TagKinds.TagKind

object TagMapper extends ((model.Tag) => Tag) {
  override def apply(tag: model.Tag): Tag = {
    Tag(
      id = Some(Tag.Id(tag.id.key, mapKind(tag.id.kind))),
      displayValue = tag.name
    )
  }

  private def mapKind(modelKind: TagKind): Tag.Kind = {
    modelKind match {
      case TagKinds.PodcastGenre => Tag.Kind.PODCAST_GENRE
      case TagKinds.PodcastCreator => Tag.Kind.PODCAST_CREATOR
      case TagKinds.MetadataFlag => Tag.Kind.METADATA_FLAG
      case _ => Tag.Kind.UNKNNOWN_TYPE
    }
  }
}
