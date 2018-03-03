package kurtome.dote.server.controllers.feed

import kurtome.dote.proto.api.feed.FeedId.{HomeId, ProfileId}
import kurtome.dote.proto.api.feed._
import kurtome.dote.slick.db.gen.Tables

import scala.concurrent.Future

trait FeedFetcher {

  def fetch(params: FeedParams): Future[Feed]

  protected def buildFeedId(id: HomeId): FeedId = {
    FeedId().withHomeId(id)
  }

  protected def buildFeedId(id: ProfileId): FeedId = {
    FeedId().withProfileId(id)
  }
}

case class FeedParams(loggedInUser: Option[Tables.PersonRow],
                      maxItemSize: Int = 10,
                      feedId: FeedId)
