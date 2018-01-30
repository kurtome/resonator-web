package kurtome.dote.server.controllers.feed

import kurtome.dote.proto.api.dotable.Dotable
import kurtome.dote.proto.api.dotable_list.DotableList
import kurtome.dote.proto.api.feed.FeedId.{HomeId, ProfileId}
import kurtome.dote.proto.api.feed._
import kurtome.dote.server.model.TagList
import kurtome.dote.slick.db.gen.Tables

import scala.concurrent.Future

trait FeedFetcher {

  def fetch(params: FeedParams): Future[Feed]

  protected def toListFeedItem(list: TagList): FeedItem = {
    val feedList = FeedDotableList(Some(DotableList(title = list.tag.name, dotables = list.list)))
    FeedItem(kind = FeedItem.Kind.DOTABLE_LIST, content = FeedItem.Content.DotableList(feedList))
  }

  protected def toListFeedItem(title: String, list: Seq[Dotable]): FeedItem = {
    val feedList = FeedDotableList(Some(DotableList(title = title, dotables = list)))
    FeedItem(kind = FeedItem.Kind.DOTABLE_LIST, content = FeedItem.Content.DotableList(feedList))
  }

  protected def buildId(id: HomeId): FeedId = {
    FeedId().withHomeId(id)
  }

  protected def buildId(id: ProfileId): FeedId = {
    FeedId().withProfileId(id)
  }
}

case class FeedParams(loggedInUser: Option[Tables.PersonRow],
                      maxItemSize: Int = 10,
                      feedId: FeedId)
