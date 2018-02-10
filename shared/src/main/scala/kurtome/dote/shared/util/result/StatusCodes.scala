package kurtome.dote.shared.util.result

/**
  * For simplicity, don't change the order of these values so server/client don't get out of sync.
  * The server sends error codes back to the client using the integer representation which is
  * determined by the order.
  */
object StatusCodes extends Enumeration {
  type StatusCode = Value
  val UnknownError = Value

  val Success = Value

  val Required = Value
  val UnderMin = Value
  val OverMax = Value
  val InvalidEmail = Value
  val InvalidItunesPodcastUrl = Value
  val InvalidUsername = Value
  val NotUnique = Value
  val NotLoggedIn = Value
  val InvalidAuthentication = Value
  val NotFound = Value
}

/**
  * Error causes link errors back to a specific part of a multi-part request.
  */
object ErrorCauses extends Enumeration {
  type ErrorCause = Value

  val UnknownCause = Value

  /**
    * This may be used either to represent that the error is general to the entire requested action,
    * or as a placeholder when there is no error.
    */
  val NoCause = Value

  val EmailAddress = Value
  val Username = Value
  val Url = Value
}

