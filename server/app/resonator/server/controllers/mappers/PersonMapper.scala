package resonator.server.controllers.mappers

import resonator.proto.api.person.Person
import resonator.server.util.UrlIds
import resonator.slick.db.gen
import resonator.slick.db.gen.Tables

object PersonMapper extends ((Tables.PersonRow) => Person) {
  override def apply(row: gen.Tables.PersonRow): Person = {
    Person(
      id = UrlIds.encode(UrlIds.IdKinds.Person, row.id),
      username = row.username,
      // don't leak email addresses everywhere that Person is returned to the client
      email = ""
    )
  }
}
