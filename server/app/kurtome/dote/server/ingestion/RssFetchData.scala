package kurtome.dote.server.ingestion

import dote.proto.db.dotable.{DotableCommon, DotableDetails}
import kurtome.dote.server.db.Tag

case class RssFetchedPodcast(feedUrl: String,
                             feedEtag: String,
                             common: DotableCommon,
                             tags: Seq[Tag],
                             details: DotableDetails.Podcast,
                             episodes: Seq[RssFetchedEpisode])
case class RssFetchedEpisode(common: DotableCommon, details: DotableDetails.PodcastEpisode)
