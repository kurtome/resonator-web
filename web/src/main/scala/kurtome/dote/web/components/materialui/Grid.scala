package kurtome.dote.web.components.materialui

import japgolly.scalajs.react
import japgolly.scalajs.react._
import japgolly.scalajs.react.component.Js.RawMounted
import japgolly.scalajs.react.component.Js.UnmountedWithRawType
import kurtome.dote.web.components.materialui.Grid.AlignItems
import kurtome.dote.web.components.materialui.Grid.Direction
import kurtome.dote.web.components.materialui.Grid.HiddenProps
import kurtome.dote.web.components.materialui.Grid.Justify
import kurtome.dote.web.components.materialui.Grid.Wrap

import scala.scalajs.js
import scala.scalajs.js.annotation._

/**
  * Wrapper for https://material-ui-next.com/api/grid/
  */
object Grid {

  @JSImport("material-ui/Grid/Grid.js", JSImport.Default)
  @js.native
  object RawComponent extends js.Object

  /**
    * https://material-ui-next.com/layout/hidden/
    */
  @js.native
  trait HiddenProps extends js.Object {
    var xsUp: js.UndefOr[Boolean] = js.native
    var xsDown: js.UndefOr[Boolean] = js.native
  }
  object HiddenProps {
    def apply(xsUp: js.UndefOr[Boolean] = js.undefined,
              xsDown: js.UndefOr[Boolean] = js.undefined): HiddenProps = {
      val p = new js.Object().asInstanceOf[HiddenProps]
      p.xsUp = xsUp
      p.xsDown = xsDown
      p
    }
  }

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
    var hidden: js.UndefOr[HiddenProps] = js.native
  }

  val component = JsComponent[Props, Children.Varargs, Null](RawComponent)

  def apply(key: Option[react.Key] = None,
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
            xs: js.UndefOr[Int] = js.undefined,
            hidden: js.UndefOr[HiddenProps] = js.undefined)
    : CtorType.Children[Props, UnmountedWithRawType[Props, Null, RawMounted]] = {
    val p = (new js.Object).asInstanceOf[Props]
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
    p.hidden = hidden

    if (key.isDefined) {
      component.withKey(key.get).withProps(p)
    } else {
      component.withProps(p)
    }
  }
}

object GridContainer {
  def apply(
      key: Option[react.Key] = None,
      style: js.UndefOr[js.Dynamic] = js.undefined,
      spacing: js.UndefOr[Int] = js.undefined,
      justify: js.UndefOr[Justify.Value] = js.undefined,
      direction: js.UndefOr[Direction.Value] = js.undefined,
      wrap: js.UndefOr[Wrap.Value] = js.undefined,
      hidden: js.UndefOr[HiddenProps] = js.undefined,
      alignItems: js.UndefOr[AlignItems.Value] = js.undefined
  ): CtorType.Children[Grid.Props, UnmountedWithRawType[Grid.Props, Null, RawMounted]] = {
    Grid(container = true,
         key = key,
         style = style,
         spacing = spacing,
         justify = justify,
         direction = direction,
         wrap = wrap,
         alignItems = alignItems,
         hidden = hidden)
  }
}

object GridItem {
  def apply(
      key: Option[react.Key] = None,
      style: js.UndefOr[js.Dynamic] = js.undefined,
      hidden: js.UndefOr[HiddenProps] = js.undefined,
      xl: js.UndefOr[Int] = js.undefined,
      lg: js.UndefOr[Int] = js.undefined,
      md: js.UndefOr[Int] = js.undefined,
      sm: js.UndefOr[Int] = js.undefined,
      xs: js.UndefOr[Int] = js.undefined
  ): CtorType.Children[Grid.Props, UnmountedWithRawType[Grid.Props, Null, RawMounted]] = {
    Grid(item = true,
         key = key,
         style = style,
         xl = xl,
         lg = lg,
         md = md,
         sm = sm,
         xs = xs,
         hidden = hidden)
  }
}
