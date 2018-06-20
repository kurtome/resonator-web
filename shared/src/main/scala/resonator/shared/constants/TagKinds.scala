package resonator.shared.constants

object TagKinds extends Enumeration {
  type TagKind = Value
  val MetadataFlag = Value("metadata_flag")
  val PodcastCreator = Value("podcast_creator")
  val PodcastGenre = Value("podcast_genre")
  val Keyword = Value("keyword")
}


