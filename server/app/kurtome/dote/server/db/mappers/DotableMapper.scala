package kurtome.dote.server.db.mappers

import com.trueaccord.scalapb.json.JsonFormat
import kurtome.dote.proto.api.dotable.Dotable
import kurtome.dote.proto.db.dotable.DotableData
import kurtome.dote.server.util.{Slug, UrlIds}
import kurtome.dote.server.util.UrlIds.IdKinds
import kurtome.dote.shared.constants.DotableKinds
import kurtome.dote.shared.constants.DotableKinds.DotableKind
import kurtome.dote.slick.db.gen.Tables

object DotableMapper extends ((Tables.DotableRow, Option[Tables.DotableRow]) => Dotable) {
  def apply(row: Tables.DotableRow, parent: Option[Tables.DotableRow]) = {
    val kind = row.kind
    val data = JsonFormat.fromJson[DotableData](row.data)

    Dotable(
      id = UrlIds.encode(IdKinds.Dotable, row.id),
      slug = Slug.slugify(data.getCommon.title),
      relatives = parent.map(relativesFromParent),
      kind = mapKind(row.kind),
      common = data.common,
      details = data.details
    )
  }

  def mapKind(rowKind: DotableKind): Dotable.Kind = {
    rowKind match {
      case DotableKinds.Podcast => Dotable.Kind.PODCAST
      case DotableKinds.PodcastEpisode => Dotable.Kind.PODCAST_EPISODE
      case DotableKinds.Review => Dotable.Kind.REVIEW
      case _ => throw new IllegalStateException("unexpected type " + rowKind)
    }
  }

  private def relativesFromParent(parent: Tables.DotableRow): Dotable.Relatives = {
    Dotable.Relatives(
      parent = Some(DotableMapper(parent, None))
    )
  }
}
