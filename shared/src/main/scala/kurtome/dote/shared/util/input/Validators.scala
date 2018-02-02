package kurtome.dote.shared.util.input

import kurtome.dote.shared.util.result._
import kurtome.dote.shared.util.result.ErrorCauses.ErrorCause
import kurtome.dote.shared.util.result.StatusCodes.StatusCode

abstract class InputValidator[TInput] extends ((TInput => StatusCode))

case class FieldValidators[TInput](cause: ErrorCause, validators: Seq[InputValidator[TInput]]) {
  def firstError(input: TInput): ActionStatus = {
    // Return the first non-successful validation, or success
    validators.toStream
      .map(v => v(input))
      .filterNot(_ == StatusCodes.Success)
      .map(ErrorStatus(cause, _))
      .headOption
      .getOrElse(SuccessStatus)
  }
}

trait SimpleValidator[T] extends InputValidator[T] {
  val errorCode: StatusCode
  def test(input: T): Boolean

  def apply(input: T) = {
    if (test(input)) {
      StatusCodes.Success
    } else {
      errorCode
    }
  }
}

final object Required extends SimpleValidator[String] {
  val errorCode = StatusCodes.Required
  def test(input: String) = input.length > 0
}

final case class MinLength(min: Int) extends SimpleValidator[String] {
  val errorCode = StatusCodes.UnderMin
  def test(input: String) = input.length >= min
}

final case class MaxLength(max: Int) extends SimpleValidator[String] {
  val errorCode = StatusCodes.OverMax
  def test(input: String) = input.length <= max
}

final object ValidEmail extends SimpleValidator[String] {
  private val emailRegex =
    """^[a-zA-Z0-9\.!#$%&'*+/=?^_`{|}~-]+@[a-zA-Z0-9](?:[a-zA-Z0-9-]{0,61}[a-zA-Z0-9])?(?:\.[a-zA-Z0-9](?:[a-zA-Z0-9-]{0,61}[a-zA-Z0-9])?)*$""".r
  val errorCode = StatusCodes.InvalidEmail
  def test(input: String) = emailRegex.findFirstIn(input).isDefined
}

final object ValidUsername extends SimpleValidator[String] {
  // Allow only letters and dashes (cannot start or end in a dash)
  private val regex = """^[a-z][-a-z]+[a-z]$""".r
  val errorCode = StatusCodes.InvalidUsername
  def test(input: String) = regex.findFirstIn(input).isDefined
}

final object ValidItunesPodcastUrl extends SimpleValidator[String] {
  private val urlRegex =
    """https:\/\/(?:www\.|(?!www))itunes.apple.com.*\/podcast[\/a-zA-Z0-9-]+""".r
  val errorCode = StatusCodes.InvalidItunesPodcastUrl
  def test(input: String) = urlRegex.findFirstIn(input).isDefined
}
