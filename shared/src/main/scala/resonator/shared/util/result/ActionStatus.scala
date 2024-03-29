package resonator.shared.util.result

import resonator.shared.util.result.ErrorCauses.ErrorCause
import resonator.shared.util.result.StatusCodes.StatusCode

sealed abstract class ActionStatus {
  val cause: ErrorCause
  val code: StatusCode
  def isSuccess = code == StatusCodes.Success
}

final object SuccessStatus extends ActionStatus {
  override val cause: ErrorCause = ErrorCauses.NoCause
  override val code: StatusCode = StatusCodes.Success
}

sealed case class ErrorStatus(cause: ErrorCause, code: StatusCode) extends ActionStatus {
  assert(code != StatusCodes.Success)
}
final object ErrorStatus {
  def  apply(code: StatusCode): ErrorStatus = ErrorStatus(ErrorCauses.NoCause, code)
}

final object UnknownErrorStatus extends ErrorStatus(ErrorCauses.UnknownCause, StatusCodes.UnknownError)
