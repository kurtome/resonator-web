package resonator.web.utils.input

import scala.collection.mutable

class InputFieldSetConfig {
  type Validator = (String) => String

  private val fieldData: mutable.Map[String, String] = mutable.Map()
  private val fieldValidators: mutable.Map[String, Seq[Validator]] = mutable.Map()

  def isFieldValid(fieldName: String): Boolean = {
    //validationResult.errors.keySet.contains(fieldName)
    ???
  }

  def validate(fieldData: Map[String, String]): Map[String, String] = {
    fieldData map {
      case (name, data) =>
        name -> fieldValidators(name)
          .map(validator => validator(data))
          .filterNot(_.isEmpty)
          .headOption
          .getOrElse("")
    }
  }
}
