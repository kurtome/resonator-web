package resonator.server.db.mappers

import resonator.proto.api.dote.Dote
import resonator.server.controllers.mappers.PersonMapper
import resonator.server.util.UrlIds
import resonator.shared.mapper.EmoteKindMapper
import resonator.slick.db.gen.Tables

object DoteMapper {
  def toProto(row: Tables.DoteRow, person: Option[Tables.PersonRow]): Dote = {
    Dote(
      person = person.map(PersonMapper),
      halfStars = row.halfStars,
      emoteKind = EmoteKindMapper.toProto(row.emoteKind),
      reviewId = row.reviewDotableId.map(UrlIds.encodeDotable).getOrElse("")
    )
  }
}
