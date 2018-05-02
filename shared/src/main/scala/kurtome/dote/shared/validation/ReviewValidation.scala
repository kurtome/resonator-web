package kurtome.dote.shared.validation

import kurtome.dote.shared.util.input._
import kurtome.dote.shared.util.result.ErrorCauses

object ReviewValidation {

  val maxLength = 1000

  val body =
    FieldValidators(ErrorCauses.NoCause, Seq(Required, MinLength(5), MaxLength(maxLength)))

}
