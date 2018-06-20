package resonator.shared.validation

import resonator.shared.util.input._
import resonator.shared.util.result.ErrorCauses

object ReviewValidation {

  val maxLength = 1000

  val body =
    FieldValidators(ErrorCauses.NoCause, Seq(Required, MinLength(5), MaxLength(maxLength)))

}
