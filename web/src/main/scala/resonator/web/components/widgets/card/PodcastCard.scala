package resonator.web.components.widgets.card

import japgolly.scalajs.react._
import japgolly.scalajs.react.vdom.DomCallbackResult
import japgolly.scalajs.react.vdom.html_<^._
import resonator.proto.api.dotable.Dotable
import resonator.web.CssSettings._
import resonator.web.DoteRoutes._
import resonator.web.components.materialui._
import resonator.web.components.ComponentHelpers._
import resonator.web.components.views.DotableDetailView
import resonator.web.constants.MuiTheme
import resonator.web.rpc.TimeCachedValue
import resonator.web.utils._
import wvlet.log.LogSupport

import scala.scalajs.js.Date

object PodcastCard extends LogSupport {

  object Styles extends StyleSheet.Inline {
    import dsl._

    val container = style(
      pointerEvents := "auto"
    )

    val activityPaper = style(
      position.relative,
      backgroundColor :=! MuiTheme.theme.palette.extras.accentPaperBackground
    )

    val defaultPaper = style(
      position.relative,
      backgroundColor :=! MuiTheme.theme.palette.background.paper
    )

  }

  private object Animations extends StyleSheet.Inline {
    import dsl._
    val fadeInImage = keyframes(
      (0 %%) -> keyframe(opacity(0)),
      (100 %%) -> keyframe(opacity(1))
    )
  }
  Animations.addToDocument()

  object Colors extends Enumeration {
    val PrimaryAccent = Value // recent activity feed
    val Default = Value
  }
  type Color = Colors.Value

  object Variants extends Enumeration {
    val ImageOnly = Value
    val Default = Value
  }
  type Variant = Variants.Value

  case class Props(dotable: Dotable, color: Color, variant: Variant, showDescription: Boolean)
  case class State()

  class Backend(bs: BackendScope[Props, State]) extends BaseBackend(Styles) {

    def backgroundColor(p: Props): StyleA = {
      p.color match {
        case Colors.PrimaryAccent => Styles.activityPaper
        case _ => Styles.defaultPaper
      }
    }

    def render(p: Props, s: State): VdomElement = {
      val id = p.dotable.id
      val slug = p.dotable.slug
      val detailRoute = DetailsRoute(id = id, slug = slug)

      val url = if (p.dotable.kind == Dotable.Kind.PODCAST_EPISODE) {
        p.dotable.getRelatives.getParent.getDetails.getPodcast.imageUrl
      } else {
        p.dotable.getDetails.getPodcast.imageUrl
      }

      val yearRange: String =
        epochSecRangeToYearRange(p.dotable.getCommon.publishedEpochSec,
                                 p.dotable.getCommon.updatedEpochSec).getOrElse("")

      <.div(
        ^.className := backgroundColor(p),
        ^.width := "100%",
        p.variant match {
          case Variants.ImageOnly =>
            <.div(
              ^.cursor.pointer,
              ^.onClick ==> ((e: ReactMouseEvent) =>
                Callback {
                  // Don't hijack clicks on anchor links
                  if (e.target.nodeName.toLowerCase != "a") {
                    DotableDetailView.cachedDotable = TimeCachedValue.minutes(1, p.dotable)
                    doteRouterCtl.set(detailRoute).runNow()
                  }
                }),
              ^.className := Styles.container,
              PodcastImageCard(dotable = p.dotable, width = "100%")()
            )
          case _ =>
            <.div(
              ImageWithSummaryCard(
                p.dotable,
                title = p.dotable.getCommon.title,
                caption1 =
                  Typography(variant = Typography.Variants.Caption, noWrap = true)(yearRange),
                description = if (p.showDescription) {
                  Typography(variant = Typography.Variants.Body2, noWrap = true)(
                    stripTags(p.dotable.getCommon.description))
                } else {
                  ""
                }
              )()
            )
        }
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
            variant: Variant = Variants.Default,
            showDescription: Boolean = false) =
    component
      .withKey(s"podcast-card-${dotable.id}")
      .withProps(Props(dotable, color, variant, showDescription))
}
