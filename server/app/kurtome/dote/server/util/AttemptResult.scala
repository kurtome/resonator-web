package kurtome.dote.server.util

sealed abstract class SideEffectResult[+TResult] {
  val result: TResult
  val effectSuccess: Boolean
  val effectErrorMessage: String
}

final case class SuccessEffect[R](result: R) extends SideEffectResult[R] {
  override val effectSuccess: Boolean = true
  override val effectErrorMessage: String = ""
}

final case class FailedEffect[R](result: R, effectErrorMessage: String)
    extends SideEffectResult[R] {
  override val effectSuccess: Boolean = false
}
