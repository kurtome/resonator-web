package kurtome.dote.server.model

import kurtome.dote.slick.db.TagKinds

/**
  * Keys for [[TagKinds.MetadataFlag]] tags.
  */
object MetadataFlag {

  object Keys {
    val popular = "popular"
    val explicit = "explicit"
  }

  object Ids {
    val popular = TagId(TagKinds.MetadataFlag, Keys.popular)
    val explicit = TagId(TagKinds.MetadataFlag, Keys.explicit)
  }

}
