package kurtome.dote.shared.validation

import kurtome.dote.shared.util.input._
import kurtome.dote.shared.util.result.ErrorCauses

object AddPodcastValidation {

  val url = FieldValidators(ErrorCauses.Url, Seq(Required, ValidItunesPodcastUrl))

}
