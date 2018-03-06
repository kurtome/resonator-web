package kurtome.dote.server.model

import kurtome.dote.shared.model.TagId
import kurtome.dote.shared.constants.TagKinds

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
