package kurtome.dote.web.components.materialui

import japgolly.scalajs.react._

import scala.scalajs.js
import scala.scalajs.js.annotation.JSImport
import scala.scalajs.js.JSConverters._

/**
  * Wrapper for https://material-ui.com/api/table-pagination/
  */
object TablePagination {

  @JSImport("@material-ui/core/TablePagination", JSImport.Default)
  @js.native
  object RawComponent extends js.Object

  @js.native
  trait Props extends js.Object {
    var count: js.UndefOr[Int] = js.native
    var page: js.UndefOr[Int] = js.native
    var labelRowsPerPage: js.UndefOr[raw.React.Element] = js.native
    var rowsPerPage: js.UndefOr[Int] = js.native
    var rowsPerPageOptions: js.UndefOr[js.Array[Int]] = js.native
    var onChangePage: js.Function2[js.Dynamic, Int, Unit] = js.native
    var onChangeRowsPerPage: js.Function1[js.Dynamic, Unit] = js.native
    var className: js.UndefOr[String] = js.native
  }

  val component = JsComponent[Props, Children.Varargs, Null](RawComponent)

  def apply(count: js.UndefOr[Int] = js.undefined,
            page: js.UndefOr[Int] = js.undefined,
            labelRowsPerPage: js.UndefOr[raw.React.Element] = js.undefined,
            rowsPerPage: js.UndefOr[Int] = js.undefined,
            rowsPerPageOptions: js.UndefOr[Array[Int]] = js.undefined,
            onChangePage: (js.Dynamic, Int) => Callback = (_, _) => Callback.empty,
            onChangeRowsPerPage: (js.Dynamic) => Callback = (_) => Callback.empty,
            className: js.UndefOr[String] = js.undefined) = {
    val p = (new js.Object).asInstanceOf[Props]
    p.count = count
    p.page = page
    p.labelRowsPerPage = labelRowsPerPage
    p.rowsPerPage = rowsPerPage
    p.rowsPerPageOptions = rowsPerPageOptions.map(_.toJSArray)
    p.onChangePage = (e: js.Dynamic, v: Int) => onChangePage(e, v).runNow()
    p.onChangeRowsPerPage = (e: js.Dynamic) => onChangeRowsPerPage(e).runNow()
    p.className = className
    component.withProps(p)
  }
}
