package resonator.web.components.widgets.card

import japgolly.scalajs.react._
import japgolly.scalajs.react.vdom.html_<^._
import resonator.proto.api.dotable.Dotable
import resonator.web.CssSettings._
import resonator.web.DoteRoutes._
import resonator.web.SharedStyles
import resonator.web.components.ComponentHelpers._
import resonator.web.components.materialui._
import resonator.web.components.views.DotableDetailView
import resonator.web.components.widgets.SiteLink
import resonator.web.constants.MuiTheme
import resonator.web.rpc.TimeCachedValue
import resonator.web.utils._
import wvlet.log.LogSupport

import scala.scalajs.js.Date

object ImageWithSummaryCard extends LogSupport {

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
      position.absolute,
      display.inlineBlock
    )

    val titleLine = style(
      marginTop(SharedStyles.spacingUnit),
      marginLeft(SharedStyles.spacingUnit),
      marginRight(SharedStyles.spacingUnit / 2),
      marginBottom(SharedStyles.spacingUnit / 2)
    )

    val imageOverlay = style(
      pointerEvents := "none",
      position.absolute,
      height(100 %%),
      // for some reason this had a small gap with width=100%
      // the overflow=hidden on the parent hides the excess
      width(101 %%)
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
                   title: String,
                   caption1: VdomNode,
                   caption2: VdomNode,
                   description: VdomNode,
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

      <.div(
        ^.className := Styles.wrapper,
        ^.width := "100%",
        ^.height := asPxStr(height),
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
          ^.height := "100%",
          ^.className := Styles.container,
          <.div(
            ^.position.relative,
            ^.display.`inline-block`,
            ^.overflow.hidden,
            ^.width := "100px",
            ^.height := "100%",
            <.div(^.className := Styles.imgWrapper,
                  PodcastImageCard(dotable = p.dotable, width = imageHeight)()),
            Hidden(xsUp = p.imageOverlayCaption.isEmpty)(<.div(
              ^.className := Styles.imageOverlay,
              <.div(
                ^.width := "100%",
                ^.height := "calc(100% - 1.5em)"
              ),
              <.div(
                ^.className := Styles.overlayText,
                ^.backgroundColor := MuiTheme.theme.palette.background.paper,
                Typography(style = Styles.fromPodcastText,
                           variant = Typography.Variants.Caption,
                           noWrap = true)(p.imageOverlayCaption)
              )
            ))
          ),
          <.div(
            ^.className := Styles.mainTextWrapper,
            ^.width := "calc(100% - 100px)",
            ^.height := "100%",
            <.div(
              ^.padding := "4px",
              ^.height := "100%",
              Hidden(xsUp = p.title.isEmpty)(
                Typography(variant = Typography.Variants.Body1, noWrap = true)(
                  <.strong(p.dotable.getCommon.title))),
              p.caption1,
              p.caption2,
              <.div(
                ^.position := "absolute",
                ^.width := "calc(100% - 8px)",
                ^.bottom := "4px",
                p.description
              )
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
    .renderPS((builder, p, s) => builder.backend.render(p, s))
    .build

  def apply(dotable: Dotable,
            title: String = "",
            caption1: VdomNode = "",
            caption2: VdomNode = "",
            description: VdomNode = "",
            imageOverlayCaption: String = "") =
    component.withProps(
      Props(dotable, title, caption1, caption2, description, imageOverlayCaption))
}
