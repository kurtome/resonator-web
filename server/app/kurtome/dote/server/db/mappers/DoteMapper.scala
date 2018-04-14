package kurtome.dote.server.db.mappers

import kurtome.dote.proto.api.dote.Dote
import kurtome.dote.server.controllers.mappers.PersonMapper
import kurtome.dote.shared.mapper.EmoteKindMapper
import kurtome.dote.slick.db.gen.Tables

object DoteMapper {
  def toProto(row: Tables.DoteRow, person: Option[Tables.PersonRow] = None): Dote = {
    Dote(
      person = person.map(PersonMapper),
      halfStars = row.halfStars,
      emoteKind = EmoteKindMapper.toProto(row.emoteKind)
    )
  }
}
