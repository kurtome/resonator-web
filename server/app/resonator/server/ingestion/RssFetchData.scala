package resonator.server.ingestion

import resonator.proto.db.dotable.{DotableCommon, DotableDetails}
import resonator.shared.model.Tag

case class RssFetchedPodcast(feedUrl: String,
                             feedEtag: Option[String],
                             dataHash: Array[Byte],
                             common: DotableCommon,
                             tags: Seq[Tag],
                             details: DotableDetails.Podcast,
                             episodes: Seq[RssFetchedEpisode])

case class RssFetchedEpisode(common: DotableCommon,
                             details: DotableDetails.PodcastEpisode,
                             dataHash: Array[Byte])
