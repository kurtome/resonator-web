package kurtome.dote.server.controllers.mappers

import dote.proto.api.person.Person
import kurtome.dote.server.util.{Slug, UrlIds}
import kurtome.dote.slick.db.gen
import kurtome.dote.slick.db.gen.Tables

object PersonMapper extends ((Tables.PersonRow) => Person) {
  override def apply(row: gen.Tables.PersonRow): Person = {
    Person(id = UrlIds.encode(UrlIds.IdKinds.Person, row.id),
           slug = Slug(row.username),
           username = row.username,
           email = row.email)
  }
}
