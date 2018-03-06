package kurtome.dote.web.utils

import kurtome.dote.proto.api.feed.FeedId
import kurtome.dote.proto.api.feed.FeedId._
import kurtome.dote.shared.constants.TagKinds
import kurtome.dote.shared.mapper.TagMapper
import kurtome.dote.web.DoteRoutes.DoteRoute
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
      case Id.TagList(TagListId(Some(protoTag), kind)) => {
        val tag = TagMapper.fromProto(protoTag)
        Some(TagRoute(TagKindUrlMapper.toUrl(tag.id.kind), tag.id.key))
      }
      case _ => None
    }
  }

  object TagKindUrlMapper {
    import TagKinds._

    def toUrl(kind: TagKind): String = {
      kind match {
        case MetadataFlag => "m"
        case PodcastCreator => "network"
        case PodcastGenre => "category"
        case Keyword => "keyword"
      }
    }

    def fromUrl(s: String): TagKind = {
      s match {
        case "m" => MetadataFlag
        case "network" => PodcastCreator
        case "category" => PodcastGenre
        case "keyword" => Keyword
      }
    }
  }

}
