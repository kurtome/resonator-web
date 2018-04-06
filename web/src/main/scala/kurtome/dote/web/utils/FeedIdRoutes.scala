package kurtome.dote.web.utils

import kurtome.dote.proto.api.dotable.Dotable
import kurtome.dote.proto.api.feed.FeedId
import kurtome.dote.proto.api.feed.FeedId._
import kurtome.dote.proto.api.tag.Tag
import kurtome.dote.shared.constants.TagKinds
import kurtome.dote.shared.mapper.TagMapper
import kurtome.dote.web.DoteRoutes.DoteRoute
import kurtome.dote.web.DoteRoutes.FollowersRoute
import kurtome.dote.web.DoteRoutes.HomeRoute
import kurtome.dote.web.DoteRoutes.ProfileRoute
import kurtome.dote.web.DoteRoutes.TagRoute

object FeedIdRoutes {

  /**
    * Routes a feed to the page that can render it.
    * @return the associated route to render the feed, or [[None]] if the feed doesn't have a detail
    *         view
    */
  def toRoute(feedId: FeedId): Option[DoteRoute] = {
    feedId.id match {
      case Id.Home(_) => Some(HomeRoute)
      case Id.Profile(ProfileId(username)) => Some(ProfileRoute(username))
      case Id.TagList(TagListId(Some(tag), kind, pageIndex)) => {
        Some(TagRouteMapper.toRoute(tag, kind, pageIndex))
      }
      case Id.FollowerSummary(FollowerSummaryId(username)) => Some(FollowersRoute(username))
      case _ => None
    }
  }

  def pageIndex(feedId: FeedId): Int = {
    feedId.id match {
      case Id.TagList(TagListId(Some(tag), kind, pageIndex)) => pageIndex
      case _ => 0
    }
  }

  def prevPageRoute(feedId: FeedId): Option[DoteRoute] = {
    feedId.id match {
      case Id.TagList(TagListId(Some(tag), kind, pageIndex)) => {
        if (pageIndex > 0) {
          Some(TagRouteMapper.toRoute(tag, kind, pageIndex - 1))
        } else {
          None
        }
      }
      case _ => None
    }
  }

  def nextPageRoute(feedId: FeedId): Option[DoteRoute] = {
    feedId.id match {
      case Id.TagList(TagListId(Some(tag), kind, pageIndex)) => {
        Some(TagRouteMapper.toRoute(tag, kind, pageIndex + 1))
      }
      case _ => None
    }
  }

  object TagRouteMapper {

    def toRoute(tag: Tag,
                kind: Dotable.Kind = Dotable.Kind.PODCAST,
                pageIndex: Int = 0): DoteRoute = {
      val urlKind = FeedIdRoutes.TagKindUrlMapper.toUrl(tag.getId.kind)

      val params: Map[String, String] = if (pageIndex > 0) {
        Map("index" -> pageIndex.toString)
      } else {
        Map.empty
      }

      kind match {
        case Dotable.Kind.PODCAST_EPISODE =>
          TagRoute(urlKind, tag.getId.key, params + ("dk" -> "episode"))
        case _ => TagRoute(urlKind, tag.getId.key, params)
      }
    }

  }

  object TagKindUrlMapper {
    import TagKinds._

    def toUrl(kind: Tag.Kind): String = {
      toUrl(TagMapper.mapKind(kind))
    }

    def toUrl(kind: TagKind): String = {
      kind match {
        case MetadataFlag => "m"
        case PodcastCreator => "creator"
        case PodcastGenre => "category"
        case Keyword => "keyword"
      }
    }

    def fromUrl(s: String): TagKind = {
      s match {
        case "m" => MetadataFlag
        case "creator" => PodcastCreator
        case "category" => PodcastGenre
        case "keyword" => Keyword
      }
    }
  }

}
