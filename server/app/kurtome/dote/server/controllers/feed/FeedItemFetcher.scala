package kurtome.dote.server.controllers.feed

import kurtome.dote.proto.api.feed._
import kurtome.dote.slick.db.gen.Tables

import scala.concurrent.Future

trait FeedItemFetcher {

  def fetch(params: FeedItemParams): Future[FeedItem]

}

case class FeedItemParams(loggedInUser: Option[Tables.PersonRow],
                          maxItemSize: Int = 10,
                          feedId: FeedItemId)
