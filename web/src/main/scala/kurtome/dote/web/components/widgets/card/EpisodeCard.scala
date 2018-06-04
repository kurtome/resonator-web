package kurtome.dote.web.components.widgets.card

import japgolly.scalajs.react._
import japgolly.scalajs.react.vdom.html_<^._
import kurtome.dote.proto.api.dotable.Dotable
import kurtome.dote.web.CssSettings._
import kurtome.dote.web.DoteRoutes._
import kurtome.dote.web.components.ComponentHelpers._
import kurtome.dote.web.components.materialui._
import kurtome.dote.web.components.widgets.DotableLink
import kurtome.dote.web.components.widgets.SiteLink
import kurtome.dote.web.constants.MuiTheme
import kurtome.dote.web.utils._
import wvlet.log.LogSupport

object EpisodeCard extends LogSupport {

  private val imageHeight = "100px"

  private object Animations extends StyleSheet.Inline {
    import dsl._
    val fadeInImage = keyframes(
      (0 %%) -> keyframe(opacity(0)),
      (100 %%) -> keyframe(opacity(1))
    )
  }
  Animations.addToDocument()

  object Styles extends StyleSheet.Inline {
    import dsl._

    val activityPaper = style(
      backgroundColor :=! MuiTheme.theme.palette.extras.accentPaperBackground
    )

    val defaultPaper = style(
      backgroundColor :=! MuiTheme.theme.palette.background.paper
    )

  }

  object Colors extends Enumeration {
    val PrimaryAccent = Value // recent activity feed
    val Default = Value
  }
  type Color = Colors.Value

  case class Props(dotable: Dotable, color: Color, showTitle: Boolean, showDescription: Boolean)
  case class State(hover: Boolean = false)

  class Backend(bs: BackendScope[Props, State]) extends BaseBackend(Styles) {

    def backgroundColor(p: Props): StyleA = {
      p.color match {
        case Colors.PrimaryAccent => Styles.activityPaper
        case _ => Styles.defaultPaper
      }
    }

    def render(p: Props, s: State): VdomElement = {
      val podcast = p.dotable.getRelatives.getParent

      <.div(
        ^.className := backgroundColor(p),
        ImageWithSummaryCard(
          p.dotable,
          title = if (p.showTitle) p.dotable.getCommon.title else "",
          caption1 = Typography(variant = Typography.Variants.Caption, noWrap = true)(
            durationSecToMin(p.dotable.getDetails.getPodcastEpisode.durationSec)),
          caption2 = Typography(variant = Typography.Variants.Caption, noWrap = true)(
            "from ",
            DotableLink(podcast)(podcast.getCommon.title)),
          description = if (p.showDescription) {
            Typography(variant = Typography.Variants.Body2, noWrap = true)(
              stripTags(p.dotable.getCommon.description))
          } else {
            ""
          },
          imageOverlayCaption = epochSecToDate(p.dotable.getCommon.publishedEpochSec)
        )()
      )
    }
  }

  val component = ScalaComponent
    .builder[Props](this.getClass.getSimpleName)
    .initialState(State())
    .backend(new Backend(_))
    .renderPS((builder, p, s) => builder.backend.render(p, s))
    .build

  def apply(dotable: Dotable,
            color: Color = Colors.Default,
            showTitle: Boolean = true,
            showDescription: Boolean = false) =
    component.withProps(Props(dotable, color, showTitle, showDescription))
}
