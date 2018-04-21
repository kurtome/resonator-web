package kurtome.dote.web.components.widgets.card

import japgolly.scalajs.react._
import japgolly.scalajs.react.vdom.DomCallbackResult
import japgolly.scalajs.react.vdom.html_<^._
import kurtome.dote.proto.api.dotable.Dotable
import kurtome.dote.web.CssSettings._
import kurtome.dote.web.DoteRoutes._
import kurtome.dote.web.components.materialui._
import kurtome.dote.web.components.ComponentHelpers._
import kurtome.dote.web.components.views.DotableDetailView
import kurtome.dote.web.constants.MuiTheme
import kurtome.dote.web.rpc.TimeCachedValue
import kurtome.dote.web.utils._
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

  object Variants extends Enumeration {
    val Activity = Value // recent activity feed
    val Default = Value
  }
  type Variant = Variants.Value

  case class Props(dotable: Dotable, variant: Variant)
  case class State()

  class Backend(bs: BackendScope[Props, State]) extends BaseBackend(Styles) {

    def backgroundColor(p: Props): StyleA = {
      p.variant match {
        case Variants.Activity => Styles.activityPaper
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
          case Variants.Activity =>
            <.div(
              ImageWithSummaryCard(
                p.dotable,
                caption1 =
                  Typography(variant = Typography.Variants.Caption, noWrap = true)(yearRange)
              )()
            )
          case _ =>
            <.div(
              ^.cursor.pointer,
              ^.onClick ==> ((e: ReactMouseEvent) =>
                Callback {
                  // Don't hijack clicks on anchor links
                  if (e.target.nodeName.toLowerCase != "a") {
                    DotableDetailView.cachedDotable =
                      TimeCachedValue(Date.now() + (1000 * 60), p.dotable)
                    doteRouterCtl.set(detailRoute).runNow()
                  }
                }),
              ^.className := Styles.container,
              PodcastImageCard(dotable = p.dotable, width = "100%")()
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

  def apply(dotable: Dotable, variant: Variant = Variants.Default) =
    component.withProps(Props(dotable, variant))
}
