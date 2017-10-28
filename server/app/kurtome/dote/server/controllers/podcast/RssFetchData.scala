package kurtome.dote.server.controllers.podcast

import dote.proto.db.dotable.{DotableCommon, DotableDetails}

case class RssFetchedPodcast(common: DotableCommon,
                             details: DotableDetails.Podcast,
                             episodes: Seq[RssFetchedEpisode])
case class RssFetchedEpisode(common: DotableCommon, details: DotableDetails.PodcastEpisode)
