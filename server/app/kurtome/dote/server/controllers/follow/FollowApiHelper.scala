package kurtome.dote.server.controllers.follow

import javax.inject._
import kurtome.dote.proto.api.follower.FollowerSummary
import kurtome.dote.server.controllers.mappers.PersonMapper
import kurtome.dote.server.services.FollowerService
import kurtome.dote.server.services.PersonService
import kurtome.dote.slick.db.gen.Tables

import scala.concurrent.ExecutionContext
import scala.concurrent.Future

@Singleton
class FollowApiHelper @Inject()(personService: PersonService, followerService: FollowerService)(
    implicit ec: ExecutionContext) {

  def getSummary(person: Option[Tables.PersonRow]): Future[FollowerSummary] = {
    person.map(getSummary).getOrElse(Future(FollowerSummary.defaultInstance))
  }

  def getSummary(person: Tables.PersonRow): Future[FollowerSummary] = {
    val personId = person.id
    val followersFuture = followerService.readFollowers(personId)
    val followeesFuture = followerService.readFollowees(personId)

    for {
      followers <- followersFuture
      followees <- followeesFuture
    } yield
      FollowerSummary(Some(PersonMapper(person)),
                      followees.map(PersonMapper),
                      followers.map(PersonMapper))
  }
}
