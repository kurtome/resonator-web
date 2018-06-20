package resonator.web.components.widgets

import japgolly.scalajs.react.BackendScope
import japgolly.scalajs.react._
import japgolly.scalajs.react.vdom.VdomElement
import japgolly.scalajs.react.vdom.html_<^._
import resonator.proto.api.dote.Dote
import resonator.shared.constants.Emojis
import resonator.web.CssSettings._
import resonator.web.DoteRoutes.ProfileRoute
import resonator.web.components.materialui._
import resonator.web.utils.BaseBackend
import scalacss.ScalaCssReact._
import wvlet.log.LogSupport

object ActivityHeadline extends LogSupport {

  object Styles extends StyleSheet.Inline {
    import dsl._

    val accountIcon = style(
      display.inlineFlex,
      marginRight(4 px)
    )

    val rating = style(
      width.unset,
      float.right
    )

    val username = style(
      display.inline,
      marginLeft(1.9 em), // left space for account icon
      lineHeight(1.75 em)
    )

    val star = style(
      fontSize(16 px)
    )

    val starsWrapper = style(
      marginRight(8 px)
    )
  }

  case class Props(dote: Dote)
  case class State()

  class Backend(bs: BackendScope[Props, State]) extends BaseBackend(Styles) {

    def renderStar(halfStars: Int, starPosition: Int) = {
      if (halfStars < ((starPosition * 2) - 1)) {
        Icons.StarOutline(style = Styles.star)
      } else if (halfStars == ((starPosition * 2) - 1)) {
        Icons.StarHalf(style = Styles.star)
      } else {
        Icons.Star(style = Styles.star)
      }
    }

    def renderStars(dote: Dote): VdomNode = {
      val halfStars = dote.halfStars
      if (halfStars > 0) {
        Typography(component = "span",
                   variant = Typography.Variants.Caption,
                   style = Styles.starsWrapper)(
          renderStar(halfStars, 1),
          renderStar(halfStars, 2),
          renderStar(halfStars, 3),
          renderStar(halfStars, 4),
          renderStar(halfStars, 5)
        )
      } else {
        <.div()
      }
    }

    def render(p: Props, s: State): VdomElement = {
      val dote = p.dote
      <.div(
        ^.width := "100%",
        GridContainer(spacing = 0, justify = Grid.Justify.SpaceBetween)(
          GridItem()(
            GridContainer(wrap = Grid.Wrap.NoWrap, alignItems = Grid.AlignItems.Center)(
              GridItem(style = Styles.accountIcon)(Icons.AccountCircle()),
              GridItem()(
                Typography(component = "span", noWrap = true)(
                  SiteLink(ProfileRoute(dote.getPerson.username))(dote.getPerson.username)))
            )
          ),
          GridItem()(
            GridContainer(wrap = Grid.Wrap.NoWrap, alignItems = Grid.AlignItems.Center)(
              GridItem()(renderStars(dote)),
              GridItem()(
                Typography(component = "span", noWrap = true)(Emojis.pickEmoji(dote.emoteKind)))
            )
          )
        )
      )
    }
  }

  val component = ScalaComponent
    .builder[Props](this.getClass.getSimpleName)
    .initialState(State())
    .backend(new Backend(_))
    .renderPS((builder, props, state) => builder.backend.render(props, state))
    .build

  def apply(dote: Dote) = {
    component.withProps(Props(dote))
  }

}
