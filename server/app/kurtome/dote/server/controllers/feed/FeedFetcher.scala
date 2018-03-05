package kurtome.dote.server.controllers.feed

import kurtome.dote.proto.api.feed._
import kurtome.dote.slick.db.gen.Tables

import scala.concurrent.Future

trait FeedFetcher {
  def fetch(params: FeedParams): Future[Feed]
}

case class FeedParams(loggedInUser: Option[Tables.PersonRow],
                      maxItemSize: Int = 10,
                      feedId: FeedId)
