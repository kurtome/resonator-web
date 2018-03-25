package kurtome.dote.web.components.widgets

import japgolly.scalajs.react._
import japgolly.scalajs.react.vdom.html_<^._
import kurtome.dote.proto.api.dotable.Dotable
import kurtome.dote.web.CssSettings._
import kurtome.dote.web.DoteRoutes._
import kurtome.dote.web.SharedStyles
import kurtome.dote.web.components.ComponentHelpers._
import kurtome.dote.web.components.materialui._
import kurtome.dote.web.constants.MuiTheme
import kurtome.dote.web.utils._
import wvlet.log.LogSupport

object EpisodeTile extends LogSupport {

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

    val wrapper = style(
      position.relative
    )

    val image = style(
      position.absolute,
      width(100 %%),
      height(100 %%),
      transform := "scale(1.1)",
      filter := "contrast(50%) blur(10px)"
    )

    val container = style(
      position.absolute,
      width(100 %%),
      height(100 %%),
      pointerEvents := "auto"
    )

    val imgWrapper = style(
      display.inlineBlock
    )

    val mainTextWrapper = style(
      marginTop(SharedStyles.spacingUnit / 2),
      marginLeft(SharedStyles.spacingUnit),
      marginRight(SharedStyles.spacingUnit),
      display.inlineBlock,
      position.absolute
    )

    val titleLine = style(
      marginLeft(SharedStyles.spacingUnit),
      marginRight(SharedStyles.spacingUnit / 2),
      marginBottom(SharedStyles.spacingUnit / 2)
    )

    val textLine = style(
      marginLeft(SharedStyles.spacingUnit),
      marginRight(SharedStyles.spacingUnit)
    )

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

  case class Props(dotable: Dotable,
                   elevation: Int,
                   width: Int,
                   disableActions: Boolean,
                   variant: Variant)
  case class State(imgLoaded: Boolean = false,
                   hover: Boolean = false,
                   smileCount: Int = 0,
                   cryCount: Int = 0,
                   laughCount: Int = 0,
                   scowlCount: Int = 0)

  class Backend(bs: BackendScope[Props, State]) extends BaseBackend(Styles) {

    def paperStyle(p: Props): StyleA = {
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

      val height = 100
      val imageSize = height
      val textMargins = 16
      val titleWidth = p.width - (imageSize + textMargins)

      Paper(elevation = if (s.hover) p.elevation * 2 else p.elevation, style = paperStyle(p))(
        <.div(
          ^.className := Styles.wrapper,
          ^.width := asPxStr(p.width),
          ^.height := asPxStr(height),
          ^.onMouseEnter --> bs.modState(_.copy(hover = true)),
          ^.onMouseLeave --> bs.modState(_.copy(hover = false)),
          doteRouterCtl.link(detailRoute)(
            ^.className := Styles.container,
            <.div(^.className := Styles.imgWrapper,
                  EntityImage(dotable = p.dotable.getRelatives.getParent,
                              width = asPxStr(imageSize))()),
            <.div(
              ^.className := Styles.mainTextWrapper,
              ^.width := asPxStr(titleWidth),
              Typography(variant = Typography.Variants.Body1, noWrap = true)(
                p.dotable.getCommon.title),
              Typography(variant = Typography.Variants.Caption, noWrap = true)(
                durationSecToMin(p.dotable.getDetails.getPodcastEpisode.durationSec)),
              Typography(variant = Typography.Variants.Caption, noWrap = true)(
                epochSecToDate(p.dotable.getCommon.publishedEpochSec))
            )
          ),
          if (p.disableActions) {
            <.div()
          } else {
            TileActionShim(p.dotable, s.hover)()
          }
        )
      )

    }
  }

  val component = ScalaComponent
    .builder[Props](this.getClass.getSimpleName)
    .initialStateFromProps(p => {
      val dote = p.dotable.getDote
      State(smileCount = dote.smileCount,
            cryCount = dote.cryCount,
            laughCount = dote.laughCount,
            scowlCount = dote.scowlCount)
    })
    .backend(new Backend(_))
    .renderPS((builder, p, s) => builder.backend.render(p, s))
    .build

  def apply(dotable: Dotable,
            elevation: Int = 5,
            width: Int = 300,
            disableActions: Boolean = false,
            variant: Variant = Variants.Default) =
    component.withProps(Props(dotable, elevation, width, disableActions, variant))
}
