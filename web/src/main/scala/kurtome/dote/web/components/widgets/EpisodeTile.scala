package kurtome.dote.web.components.widgets

import japgolly.scalajs.react._
import japgolly.scalajs.react.vdom.html_<^._
import kurtome.dote.proto.api.dotable.Dotable
import kurtome.dote.web.CssSettings._
import kurtome.dote.web.DoteRoutes._
import kurtome.dote.web.SharedStyles
import kurtome.dote.web.components.ComponentHelpers._
import kurtome.dote.web.components.materialui._
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

  private object Styles extends StyleSheet.Inline with MuiInlineStyleSheet {
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
      display.inlineBlock,
      position.absolute
    )

    val titleLine = style(
      marginLeft(SharedStyles.spacingUnit),
      marginRight(SharedStyles.spacingUnit / 2),
      marginBottom(SharedStyles.spacingUnit / 2),
      overflow.hidden,
      display.block,
      lineHeight(1.5 em),
      minHeight(3 em),
      maxHeight(3 em)
    )

    val textLine = style(
      marginLeft(SharedStyles.spacingUnit),
      marginRight(SharedStyles.spacingUnit),
      whiteSpace.nowrap,
      overflow.hidden,
      textOverflow := "ellipsis"
    )

    val paperContainer = style(
      backgroundColor.white,
      display.inlineBlock
    )
  }
  Styles.addToDocument()
  import Styles.richStyle

  case class Props(dotable: Dotable, elevation: Int, width: Int)
  case class State(imgLoaded: Boolean = false,
                   hover: Boolean = false,
                   smileCount: Int = 0,
                   cryCount: Int = 0,
                   laughCount: Int = 0,
                   scowlCount: Int = 0)

  class Backend(bs: BackendScope[Props, State]) extends LogSupport {

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
      val titleWidth = p.width - imageSize

      Paper(elevation = if (s.hover) p.elevation * 2 else p.elevation,
            style = Styles.paperContainer.inline)(
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
              ^.height := asPxStr(imageSize),
              Typography(typographyType = Typography.Type.Body1, style = Styles.titleLine.inline)(
                p.dotable.getCommon.title),
              Typography(typographyType = Typography.Type.Caption, style = Styles.textLine.inline)(
                durationSecToMin(p.dotable.getDetails.getPodcastEpisode.durationSec)),
              Typography(typographyType = Typography.Type.Caption, style = Styles.textLine.inline)(
                epochSecToDate(p.dotable.getCommon.publishedEpochSec))
            )
          ),
          TileActionShim(p.dotable, s.hover)()
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

  def apply(dotable: Dotable, elevation: Int = 6, width: Int = 300) =
    component.withProps(Props(dotable, elevation, width))
}
