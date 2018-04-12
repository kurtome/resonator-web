package kurtome.dote.web.components.widgets.card

import japgolly.scalajs.react._
import japgolly.scalajs.react.vdom.html_<^._
import kurtome.dote.proto.api.dotable.Dotable
import kurtome.dote.web.CssSettings._
import kurtome.dote.web.DoteRoutes._
import kurtome.dote.web.components.ComponentHelpers._
import kurtome.dote.web.components.materialui._
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

  object Variants extends Enumeration {
    val Activity = Value // recent activity feed
    val Default = Value
  }
  type Variant = Variants.Value

  case class Props(dotable: Dotable, variant: Variant)
  case class State(hover: Boolean = false)

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

      val podcast = p.dotable.getRelatives.getParent
      val podcastRoute = DetailsRoute(id = podcast.id, slug = podcast.slug)

      val url = if (p.dotable.kind == Dotable.Kind.PODCAST_EPISODE) {
        podcast.getDetails.getPodcast.imageUrl
      } else {
        p.dotable.getDetails.getPodcast.imageUrl
      }

      val height = 100
      val imageSize = height
      val textMargins = 16

      <.div(
        ^.className := backgroundColor(p),
        ImageWithSummaryCard(
          p.dotable,
          caption1 = Typography(variant = Typography.Variants.Caption, noWrap = true)(
            durationSecToMin(p.dotable.getDetails.getPodcastEpisode.durationSec)),
          caption2 = Typography(variant = Typography.Variants.Caption, noWrap = true)(
            "from ",
            SiteLink(podcastRoute)(podcast.getCommon.title)),
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

  def apply(dotable: Dotable, variant: Variant = Variants.Default) =
    component.withProps(Props(dotable, variant))
}
