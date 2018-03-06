package kurtome.dote.shared.mapper

import kurtome.dote.proto.api.tag.Tag
import kurtome.dote.shared.model
import kurtome.dote.shared.constants.TagKinds
import kurtome.dote.shared.constants.TagKinds.TagKind

object TagMapper {

  def toProto(tag: model.Tag): Tag = {
    Tag(
      id = Some(Tag.Id(tag.id.key, mapKind(tag.id.kind))),
      displayValue = tag.name
    )
  }

  def fromProto(tag: Tag): model.Tag = {
    model.Tag(
      key = tag.getId.key,
      kind = mapKind(tag.getId.kind),
      name = tag.displayValue
    )

  }

  def mapKind(modelKind: TagKind): Tag.Kind = {
    modelKind match {
      case TagKinds.PodcastGenre   => Tag.Kind.PODCAST_GENRE
      case TagKinds.PodcastCreator => Tag.Kind.PODCAST_CREATOR
      case TagKinds.MetadataFlag   => Tag.Kind.METADATA_FLAG
      case _                       => Tag.Kind.UNKNNOWN_TYPE
    }
  }

  def mapKind(kind: Tag.Kind): TagKind = {
    kind match {
      case Tag.Kind.PODCAST_CREATOR => TagKinds.PodcastCreator
      case Tag.Kind.METADATA_FLAG   => TagKinds.MetadataFlag
      case Tag.Kind.PODCAST_GENRE   => TagKinds.PodcastGenre
      case _                        => throw new IllegalArgumentException(s"Unexpected kind $kind")
    }
  }
}
