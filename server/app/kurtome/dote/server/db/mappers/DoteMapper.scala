package kurtome.dote.server.db.mappers

import kurtome.dote.proto.api.dote.Dote
import kurtome.dote.slick.db.gen.Tables

object DoteMapper extends ((Tables.DoteRow) => Dote) {
  override def apply(row: Tables.DoteRow): Dote = {
    Dote(
      smileCount = row.smileCount,
      cryCount = row.cryCount,
      laughCount = row.laughCount,
      scowlCount = row.scowlCount
    )
  }
}
