package resonator.shared.constants

object DotableKinds extends Enumeration {
  type DotableKind = Value
  val Podcast = Value("podcast")
  val PodcastEpisode = Value("podcast_episode")
  val Review = Value("review")
}
