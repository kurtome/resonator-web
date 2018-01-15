package kurtome.dote.web.components.materialui

import japgolly.scalajs.react
import japgolly.scalajs.react._

import scala.scalajs.js
import scala.scalajs.js.annotation._
import scalacss.internal.StyleA

/**
  * Wrapper for https://material-ui-next.com/api/grid/
  */
object Grid {

  @JSImport("material-ui/Grid/Grid.js", JSImport.Default)
  @js.native
  object RawComponent extends js.Object

  object Wrap extends Enumeration {
    val Wrap = Value("wrap")
    val NoWrap = Value("nowrap")
    val WrapReverse = Value("wrap-reverse")
  }

  object AlignItems extends Enumeration {
    val FlexStart = Value("flex-start")
    val Center = Value("center")
    val FlexEnd = Value("flex-end")
    val Stretch = Value("stretch")
    val Baseline = Value("baseline")
  }

  object Justify extends Enumeration {
    val FlexStart = Value("flex-start")
    val Center = Value("center")
    val FlexEnd = Value("flex-end")
    val SpaceBetween = Value("space-between")
    val SpaceAround = Value("space-around")
  }

  object Direction extends Enumeration {
    val Row = Value("row")
    val RowReverse = Value("row-reverse")
    val Column = Value("column")
    val ColumnReverse = Value("column-reverse")
  }

  // NOTE: not all props exposed
  @js.native
  trait Props extends js.Object {
    var className: js.UndefOr[String] = js.native
    var style: js.UndefOr[js.Dynamic] = js.native
    var alignItems: js.UndefOr[String] = js.native
    var justify: js.UndefOr[String] = js.native
    var direction: js.UndefOr[String] = js.native
    var wrap: js.UndefOr[String] = js.native
    var container: js.UndefOr[Boolean] = js.native
    var item: js.UndefOr[Boolean] = js.native
    var spacing: js.UndefOr[Int] = js.native
    var xs: js.UndefOr[Int] = js.native
    var sm: js.UndefOr[Int] = js.native
    var md: js.UndefOr[Int] = js.native
    var lg: js.UndefOr[Int] = js.native
  }

  val component = JsComponent[Props, Children.Varargs, Null](RawComponent)

  def apply(key: Option[react.Key] = None,
            className: js.UndefOr[String] = js.undefined,
            style: js.UndefOr[js.Dynamic] = js.undefined,
            container: js.UndefOr[Boolean] = js.undefined,
            item: js.UndefOr[Boolean] = js.undefined,
            spacing: js.UndefOr[Int] = js.undefined,
            justify: js.UndefOr[Justify.Value] = js.undefined,
            direction: js.UndefOr[Direction.Value] = js.undefined,
            wrap: js.UndefOr[Wrap.Value] = js.undefined,
            alignItems: js.UndefOr[AlignItems.Value] = js.undefined,
            xl: js.UndefOr[Int] = js.undefined,
            lg: js.UndefOr[Int] = js.undefined,
            md: js.UndefOr[Int] = js.undefined,
            sm: js.UndefOr[Int] = js.undefined,
            xs: js.UndefOr[Int] = js.undefined) = {
    val p = (new js.Object).asInstanceOf[Props]
    p.className = className
    p.style = style
    p.container = container
    p.item = item
    p.spacing = spacing
    p.xs = xs
    p.sm = sm
    p.md = md
    p.lg = lg
    p.alignItems = alignItems.map(_.toString)
    p.justify = justify.map(_.toString)
    p.direction = direction.map(_.toString)
    p.wrap = wrap.map(_.toString)

    if (key.isDefined) {
      component.withKey(key.get).withProps(p)
    } else {
      component.withProps(p)
    }
  }
}
