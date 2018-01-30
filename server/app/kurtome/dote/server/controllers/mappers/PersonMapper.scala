package kurtome.dote.server.controllers.mappers

import kurtome.dote.proto.api.person.Person
import kurtome.dote.server.util.UrlIds
import kurtome.dote.slick.db.gen
import kurtome.dote.slick.db.gen.Tables

object PersonMapper extends ((Tables.PersonRow) => Person) {
  override def apply(row: gen.Tables.PersonRow): Person = {
    Person(id = UrlIds.encode(UrlIds.IdKinds.Person, row.id),
           username = row.username,
           email = row.email)
  }
}
