package kurtome.dote.shared.util.result

import StatusCodes.StatusCode

/**
  * An effect result which also produces some data.
  */
sealed abstract class ProduceAction[+TData] {
  val data: TData
  val status: ActionStatus

  def isSuccess: Boolean = status.isSuccess
}

final case class SuccessData[+T](data: T) extends ProduceAction[T] {
  override val status: ActionStatus = SuccessStatus
}

final case class FailedData[+T](data: T, status: ActionStatus) extends ProduceAction[T]
final object FailedData {
  def apply[T](data: T, code: StatusCode): FailedData[T] = {
    FailedData(data, ErrorStatus(code))
  }
}



