package resonator.shared.validation

import resonator.shared.util.input._
import resonator.shared.util.result.ErrorCauses

object LoginFieldsValidation {

  val username =
    FieldValidators(ErrorCauses.Username, Seq(Required, MinLength(4), MaxLength(30), ValidUsername))

  val email =
    FieldValidators(ErrorCauses.EmailAddress, Seq(Required, ValidEmail))

}
