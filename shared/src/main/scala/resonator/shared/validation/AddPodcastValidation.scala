package resonator.shared.validation

import resonator.shared.util.input._
import resonator.shared.util.result.ErrorCauses

object AddPodcastValidation {

  val url = FieldValidators(ErrorCauses.Url, Seq(Required, ValidItunesPodcastUrl))

}
