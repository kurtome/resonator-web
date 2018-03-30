package kurtome.dote.web.components.widgets.card

import japgolly.scalajs.react._
import japgolly.scalajs.react.vdom.html_<^._
import kurtome.dote.proto.api.dotable.Dotable
import kurtome.dote.web.CssSettings._
import kurtome.dote.web.DoteRoutes._
import kurtome.dote.web.SharedStyles
import kurtome.dote.web.components.ComponentHelpers._
import kurtome.dote.web.components.materialui._
import kurtome.dote.web.components.widgets.SiteLink
import kurtome.dote.web.constants.MuiTheme
import kurtome.dote.web.utils._
import wvlet.log.LogSupport

object ImageWithSummaryCard extends LogSupport {

//  private val imageHeight = "calc(100px - 1.5em)"
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

    val wrapper = style(
      position.relative
    )

    val container = style(
      position.absolute,
      width(100 %%),
      height(100 %%),
      pointerEvents := "auto"
    )

    val imgWrapper = style(
      display.inlineBlock,
      position.absolute
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

    val imageOverlay = style(
      pointerEvents := "none",
      position.absolute,
      zIndex(1),
      height(100 px),
      width(100 px)
    )

    val fromPodcastText = style(
      textAlign.center,
      lineHeight(2 em),
      opacity(1),
      textDecoration := "none"
    )

    val overlayText = style(
      opacity(0.8),
      height(1.5 em)
    )

  }

  object Variants extends Enumeration {
    val Activity = Value // recent activity feed
    val Default = Value
  }
  type Variant = Variants.Value

  case class Props(dotable: Dotable,
                   width: Int,
                   caption1: String,
                   caption2: String,
                   imageOverlayCaption: String)
  case class State(smileCount: Int = 0,
                   cryCount: Int = 0,
                   laughCount: Int = 0,
                   scowlCount: Int = 0)

  class Backend(bs: BackendScope[Props, State]) extends BaseBackend(Styles) {

    def render(p: Props, s: State): VdomElement = {
      val id = p.dotable.id
      val slug = p.dotable.slug
      val detailRoute = DetailsRoute(id = id, slug = slug)

      val podcast = p.dotable.getRelatives.getParent
      val podcastRoute = DetailsRoute(id = podcast.id, slug = podcast.slug)

      val height = 100
      val imageSize = height
      val textMargins = 16
      val titleWidth = p.width - (imageSize + textMargins)

      <.div(
        ^.className := Styles.wrapper,
        ^.width := "100%",
        ^.height := asPxStr(height),
        doteRouterCtl.link(detailRoute)(
          ^.height := "100%",
          ^.className := Styles.container,
          <.div(^.className := Styles.imgWrapper,
                PodcastImageCard(dotable = p.dotable, width = imageHeight)()),
          Hidden(xsUp = p.imageOverlayCaption.isEmpty)(<.div(
            ^.className := Styles.imageOverlay,
            <.div(
              ^.width := "100px",
              ^.height := "calc(100px - 1.5em)"
            ),
            <.div(
              ^.className := Styles.overlayText,
              ^.backgroundColor := MuiTheme.theme.palette.background.paper,
              Typography(style = Styles.fromPodcastText,
                         variant = Typography.Variants.Caption,
                         noWrap = true)(p.imageOverlayCaption)
            )
          )),
          <.div(^.display := "inline-block",
                ^.width := "100px",
                ^.height := "calc(100px - 1.5em)"),
          <.div(
            ^.className := Styles.mainTextWrapper,
            ^.width := asPxStr(titleWidth),
            Typography(variant = Typography.Variants.Body1, noWrap = true)(
              p.dotable.getCommon.title),
            Typography(variant = Typography.Variants.Caption, noWrap = true)(p.caption1),
            Typography(variant = Typography.Variants.Caption, noWrap = true)(p.caption2)
          )
        )
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
            width: Int = 300,
            caption1: String = "",
            caption2: String = "",
            imageOverlayCaption: String = "") =
    component.withProps(Props(dotable, width, caption1, caption2, imageOverlayCaption))
}
