package kurtome.dote.shared.validation

import kurtome.dote.shared.util.input._
import kurtome.dote.shared.util.result.ErrorCauses

object LoginFieldsValidation {

  val username =
    FieldValidators(ErrorCauses.Username, Seq(Required, MinLength(4), MaxLength(15), ValidUsername))

  val email =
    FieldValidators(ErrorCauses.EmailAddress, Seq(Required, ValidEmail))

}
