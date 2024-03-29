package resonator.web.utils

import resonator.proto.api.dotable.Dotable
import resonator.proto.api.feed.FeedId
import resonator.proto.api.feed.FeedId._
import resonator.proto.api.feed.PaginationInfo
import resonator.proto.api.tag.Tag
import resonator.shared.constants.QueryParamKeys
import resonator.shared.constants.TagKinds
import resonator.shared.mapper.TagMapper
import resonator.web.DoteRoutes.AllActivityRoute
import resonator.web.DoteRoutes.DoteRoute
import resonator.web.DoteRoutes.FollowersRoute
import resonator.web.DoteRoutes.FollowingActivityRoute
import resonator.web.DoteRoutes.HomeRoute
import resonator.web.DoteRoutes.ProfileActivityRoute
import resonator.web.DoteRoutes.ProfileRoute
import resonator.web.DoteRoutes.TagRoute

object FeedIdRoutes {

  /**
    * Routes a feed to the page that can render it.
    * @return the associated route to render the feed, or [[None]] if the feed doesn't have a detail
    *         view
    */
  def toRoute(feedId: FeedId): Option[DoteRoute] = {
    feedId.id match {
      case Id.Home(_) => Some(HomeRoute())
      case Id.Profile(ProfileId(username)) => Some(ProfileRoute(username))
      case Id.TagList(tagListId) => {
        Some(TagRouteMapper.toRoute(tagListId))
      }
      case Id.FollowerSummary(FollowerSummaryId(username)) => Some(FollowersRoute(username))
      case Id.Activity(activityId) => Some(ActivityRouteMapper.toRoute(activityId))
      case _ => None
    }
  }

  def pageIndex(feedId: FeedId): Int = {
    feedId.id match {
      case Id.TagList(TagListId(_, _, Some(paginationInfo))) => paginationInfo.pageIndex
      case Id.Activity(ActivityId(Some(paginationInfo), _, _, _)) => paginationInfo.pageIndex
      case _ => 0
    }
  }

  def prevPageRoute(feedId: FeedId): Option[DoteRoute] = {
    feedId.id match {
      case Id.TagList(tagListId) => {
        val info = tagListId.getPaginationInfo
        if (info.pageIndex > 0) {
          Some(
            TagRouteMapper.toRoute(
              tagListId.withPaginationInfo(prevPage(tagListId.getPaginationInfo))))
        } else {
          None
        }
      }
      case Id.Activity(activityId) => {
        if (activityId.getPaginationInfo.pageIndex > 0) {
          Some(
            ActivityRouteMapper.toRoute(
              activityId.withPaginationInfo(prevPage(activityId.getPaginationInfo))))
        } else {
          None
        }
      }
      case _ => None
    }
  }

  private def prevPage(paginationInfo: PaginationInfo): PaginationInfo = {
    paginationInfo.withPageIndex(paginationInfo.pageIndex - 1)
  }

  private def nextPage(paginationInfo: PaginationInfo): PaginationInfo = {
    paginationInfo.withPageIndex(paginationInfo.pageIndex + 1)
  }

  def nextPageRoute(feedId: FeedId): Option[DoteRoute] = {
    feedId.id match {
      case Id.TagList(tagListId) => {
        Some(
          TagRouteMapper.toRoute(
            tagListId.withPaginationInfo(nextPage(tagListId.getPaginationInfo))))
      }
      case Id.Activity(activityId) => {
        Some(
          ActivityRouteMapper.toRoute(
            activityId.withPaginationInfo(nextPage(activityId.getPaginationInfo))))
      }
      case _ => None
    }
  }

  object ActivityRouteMapper {

    def toRoute(activityId: ActivityId): DoteRoute = {
      val params: Map[String, String] = if (activityId.getPaginationInfo.pageIndex > 0) {
        Map(QueryParamKeys.pageIndex -> activityId.getPaginationInfo.pageIndex.toString)
      } else {
        Map.empty
      } ++ (activityId.dotableKind match {
        case Dotable.Kind.PODCAST_EPISODE => Map(QueryParamKeys.dotableKind -> "episode")
        case _ => Map.empty
      })

      if (activityId.followingOnly) {
        FollowingActivityRoute(params)
      } else if (activityId.username.nonEmpty) {
        ProfileActivityRoute(activityId.username, params)
      } else {
        AllActivityRoute(params)
      }
    }
  }

  object TagRouteMapper {

    def toRoute(tagListId: TagListId): DoteRoute = {
      val urlKind = FeedIdRoutes.TagKindUrlMapper.toUrl(tagListId.getTag.getId.kind)

      val params: Map[String, String] = if (tagListId.getPaginationInfo.pageIndex > 0) {
        Map(QueryParamKeys.pageIndex -> tagListId.getPaginationInfo.pageIndex.toString)
      } else {
        Map.empty
      }

      tagListId.dotableKind match {
        case Dotable.Kind.PODCAST_EPISODE =>
          TagRoute(urlKind,
                   tagListId.getTag.getId.key,
                   params + (QueryParamKeys.dotableKind -> "episode"))
        case _ => TagRoute(urlKind, tagListId.getTag.getId.key, params)
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
