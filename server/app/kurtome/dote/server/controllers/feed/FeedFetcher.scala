package kurtome.dote.server.controllers.feed

import kurtome.dote.proto.api.feed._
import kurtome.dote.slick.db.gen.Tables

import scala.concurrent.Future

trait FeedFetcher {
  def fetch(params: FeedParams): Future[Feed]

  protected def validFeedItem(feedItem: FeedItem): Boolean = {
    feedItem.content match {
      case FeedItem.Content.DotableList(feedDotableList) => {
        feedDotableList.getList.dotables.nonEmpty
      }
      case FeedItem.Content.TagCollection(feedTagCollection) => {
        feedTagCollection.getTagCollection.tags.nonEmpty
      }
      case FeedItem.Content.ActivityList(activityList) => {
        activityList.getActivityList.items.nonEmpty
      }
      case _ => true
    }
  }

}

case class FeedParams(loggedInUser: Option[Tables.PersonRow],
                      maxItemSize: Int = 10,
                      feedId: FeedId)
