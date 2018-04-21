package kurtome.dote.server.db.mappers

import kurtome.dote.proto.api.dote.Dote
import kurtome.dote.server.controllers.mappers.PersonMapper
import kurtome.dote.server.util.UrlIds
import kurtome.dote.shared.mapper.EmoteKindMapper
import kurtome.dote.slick.db.gen.Tables

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
