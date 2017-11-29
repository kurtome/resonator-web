package kurtome.dote.web.utils

import scala.scalajs.js
import scala.scalajs.js.annotation.JSImport

/**
  * Debounces functions so they are only called once if repeatedly invoked.
  * https://github.com/component/debounce
  *
  * Creates and returns a new debounced version of the passed function that will postpone its
  * execution until after wait milliseconds have elapsed since the last time it was invoked.
  *
  * Pass true for the immediate parameter to cause debounce to trigger the function on the leading
  * edge instead of the trailing edge of the wait interval. Useful in circumstances like preventing
  * accidental double-clicks on a "submit" button from firing a second time.
  *
  * The debounced function returned has a property 'clear' that is a function that will clear any
  * scheduled future executions of your function.
  *
  * The debounced function returned has a property 'flush' that is a function that will immediately
  * execute the function if and only if execution is scheduled, and reset the execution timer for
  * subsequent invocations of the debounced function.
  */
object Debounce {

  @JSImport("debounce", JSImport.Default)
  @js.native
  private object RawApi extends js.Object {
    def apply(fn: js.Function0[Unit],
              waitMs: Integer,
              immediate: js.UndefOr[Boolean]): js.Function0[Unit] = js.native

    def apply[T](fn: js.Function1[T, Unit],
                 waitMs: Integer,
                 immediate: js.UndefOr[Boolean]): js.Function1[T, Unit] = js.native
  }

  def debounce0(waitMs: Integer, immediate: Boolean = false)(fn: () => Unit): () => Unit =
    RawApi(fn, waitMs, immediate)

  def debounce1[T](waitMs: Integer, immediate: Boolean = false)(fn: (T) => Unit): (T) => Unit =
    RawApi(fn, waitMs, immediate)

}
