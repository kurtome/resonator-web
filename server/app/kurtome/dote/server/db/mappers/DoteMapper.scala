package kurtome.dote.server.db.mappers

import kurtome.dote.proto.api.dote.Dote
import kurtome.dote.server.controllers.mappers.PersonMapper
import kurtome.dote.slick.db.gen.Tables

object DoteMapper {
  def toProto(row: Tables.DoteRow, person: Option[Tables.PersonRow] = None): Dote = {
    Dote(
      smileCount = row.smileCount,
      cryCount = row.cryCount,
      laughCount = row.laughCount,
      scowlCount = row.scowlCount,
      person = person.map(PersonMapper)
    )
  }
}
