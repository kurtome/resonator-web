package resonator.server.model

import resonator.shared.model.TagId
import resonator.shared.constants.TagKinds

/**
  * Keys for [[TagKinds.MetadataFlag]] tags.
  */
object MetadataFlag {

  object Keys extends Enumeration {
    val popular = Value("popular")
    val trending = Value("trending")
    val explicit = Value("explicit")
  }

  object Ids {
    val popular = TagId(TagKinds.MetadataFlag, Keys.popular.toString)
    val trending = TagId(TagKinds.MetadataFlag, Keys.trending.toString)
    val explicit = TagId(TagKinds.MetadataFlag, Keys.explicit.toString)
  }

}
