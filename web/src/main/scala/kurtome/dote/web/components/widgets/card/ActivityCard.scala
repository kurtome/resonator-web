package kurtome.dote.web.components.widgets.card

import japgolly.scalajs.react.BackendScope
import japgolly.scalajs.react._
import japgolly.scalajs.react.vdom.VdomElement
import japgolly.scalajs.react.vdom.html_<^._
import kurtome.dote.proto.api.activity.Activity
import kurtome.dote.proto.api.dotable.Dotable
import kurtome.dote.shared.constants.Emojis
import kurtome.dote.web.CssSettings._
import kurtome.dote.web.DoteRoutes.ProfileRoute
import kurtome.dote.web.SharedStyles
import kurtome.dote.web.components.ComponentHelpers._
import kurtome.dote.web.components.materialui._
import kurtome.dote.web.components.widgets._
import kurtome.dote.web.constants.MuiTheme
import kurtome.dote.web.utils.BaseBackend
import scalacss.ScalaCssReact._
import wvlet.log.LogSupport

object ActivityCard extends LogSupport {

  object Styles extends StyleSheet.Inline {
    import dsl._

    val title = style(
      marginBottom(SharedStyles.spacingUnit)
    )

    val paperWrapper = style(
      backgroundColor :=! MuiTheme.theme.palette.extras.cardHeader
    )

    val headerTextWrapper = style(
      width :=! ("calc(100% - 16px)"), // leave space for the padding
      paddingTop(SharedStyles.spacingUnit),
      paddingLeft(SharedStyles.spacingUnit),
      paddingRight(SharedStyles.spacingUnit),
      display.inlineBlock
    )

    val tileContainer = style(
      marginTop(0 px)
    )

    val accountIcon = style(
      float.left
    )

    val ratingText = style(
      float.right
    )

    val username = style(
      marginLeft(1.9 em), // left space for account icon
      lineHeight(1.75 em)
    )
  }
  Styles.addToDocument()

  case class Props(activity: Activity)
  case class State()

  class Backend(bs: BackendScope[Props, State]) extends BaseBackend(Styles) {

    def render(p: Props, s: State): VdomElement = {
      val activity = p.activity
      val dotable = activity.getDote.getDotable
      val dote = activity.getDote.getDote
      val smile = Emojis.smileEmojis.lift(dote.smileCount - 1).getOrElse("")
      val cry = Emojis.cryEmojis.lift(dote.cryCount - 1).getOrElse("")
      val laugh = Emojis.laughEmojis.lift(dote.laughCount - 1).getOrElse("")
      val scowl = Emojis.scowlEmojis.lift(dote.scowlCount - 1).getOrElse("")
      <.div(
        ^.width := "100%",
        <.div(
          ^.className := Styles.headerTextWrapper,
          Typography(style = Styles.accountIcon)(Icons.AccountCircle()),
          Typography(noWrap = true, style = Styles.ratingText)(s"$smile$cry$laugh$scowl"),
          Typography(noWrap = true, style = Styles.username)(
            SiteLink(ProfileRoute(dote.getPerson.username))(dote.getPerson.username))
        ),
        if (dotable.kind == Dotable.Kind.PODCAST) {
          PodcastCard(dotable = dotable,
                      elevation = 0,
                      disableActions = true,
                      variant = PodcastCard.Variants.Activity)()
        } else if (dotable.kind == Dotable.Kind.PODCAST_EPISODE) {
          EpisodeCard(dotable = dotable,
                      elevation = 0,
                      disableActions = true,
                      variant = EpisodeCard.Variants.Activity)()
        } else {
          // Placeholder for correct spacing
          <.div(^.width := "100%")
        }
      )
    }
  }

  val component = ScalaComponent
    .builder[Props](this.getClass.getSimpleName)
    .initialState(State())
    .backend(new Backend(_))
    .renderPS((builder, props, state) => builder.backend.render(props, state))
    .build

  def apply(activity: Activity) = {
    assert(activity.content.isDote)
    component.withProps(Props(activity))
  }

}
