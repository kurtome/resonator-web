package kurtome.dote.web.components.widgets

import dote.proto.api.dotable.Dotable
import japgolly.scalajs.react._
import japgolly.scalajs.react.vdom.html_<^._
import kurtome.dote.shared.constants.StringValues
import kurtome.dote.web.SharedStyles
import kurtome.dote.web.DoteRoutes.{DoteRouterCtl, PodcastRoute}
import kurtome.dote.web.components.materialui._
import kurtome.dote.web.components.ComponentHelpers._
import kurtome.dote.web.CssSettings._
import kurtome.dote.web.components.widgets.button.emote._
import kurtome.dote.web.utils.MuiInlineStyleSheet

object EntityTile {

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

    val container = style(
      position.absolute
    )

    val imageContainer = style(
      pointerEvents := auto
    )

    val overlayContainer = style(
      position.absolute,
      pointerEvents := none,
      width(100 %%),
      height(100 %%)
    )

    val overlayActionsContainer = style(
      width(100 %%),
      height(100 %%)
    )

    val overlay = style(
      position.absolute,
      backgroundColor(rgba(255, 255, 255, 0.4)),
      width(100 %%),
      height(100 %%)
    )

    val nestedImg = style(
      position.absolute,
      animation := s"${Animations.fadeInImage.name.value} 1s",
      width(100 %%)
    )

    val placeholder = style(
      position.absolute,
      backgroundColor(rgb(200, 200, 200)),
      width(100 %%),
      // Use padding top to force the height of the div to match the width
      paddingTop(100 %%)
    )
  }
  Styles.addToDocument()
  import Styles.richStyle

  case class Props(routerCtl: DoteRouterCtl,
                   dotable: Dotable,
                   elevation: Int = 4,
                   width: String = "175px")
  case class State(imgLoaded: Boolean = false,
                   hover: Boolean = false,
                   likeCount: Int = 0,
                   sadCount: Int = 0,
                   laughCount: Int = 0)

  class Backend(bs: BackendScope[Props, State]) {
    def render(p: Props, s: State): VdomElement = {
      val id = p.dotable.id
      val slug = p.dotable.slug
      val detailRoute = PodcastRoute(id = id, slug = slug)

      val url = if (p.dotable.kind == Dotable.Kind.PODCAST_EPISODE) {
        p.dotable.getRelatives.getParent.getDetails.getPodcast.imageUrl
      } else {
        p.dotable.getDetails.getPodcast.imageUrl
      }

      val handleLikeValueChanged = (value: Int) => bs.modState(s => s.copy(likeCount = value))

      val handleSadValueChanged = (value: Int) => bs.modState(s => s.copy(sadCount = value))

      val handleLaughValueChanged = (value: Int) => bs.modState(s => s.copy(laughCount = value))

      Paper(elevation = if (s.hover) p.elevation * 2 else p.elevation,
            className = SharedStyles.inlineBlock)(
        <.div(
          ^.className := Styles.wrapper,
          ^.width := p.width,
          ^.height := p.width,
          ^.onMouseEnter --> bs.modState(_.copy(hover = true)),
          ^.onMouseLeave --> bs.modState(_.copy(hover = false)),
          <.div(
            ^.className := Styles.container,
            ^.width := p.width,
            ^.height := p.width,
            p.routerCtl.link(detailRoute)(
              <.div(
                ^.className := Styles.imageContainer,
                // placeholder div while loading, to fill the space
                <.div(
                  ^.className := Styles.placeholder
                ),
                if (url.nonEmpty) {
                  <.img(
                    ^.className := Styles.nestedImg,
                    ^.visibility := (if (s.imgLoaded) "visible" else "hidden"),
                    ^.onLoad --> bs.modState(_.copy(imgLoaded = true)),
                    ^.src := url
                  )
                } else {
                  <.div()
                }
              )
            ),
            <.div(
              ^.className := Styles.overlayContainer,
              Fade(in = s.hover, timeoutMs = 500)(
                <.div(
                  ^.className := Styles.overlayActionsContainer,
                  <.div(^.className := Styles.overlay),
                  Grid(container = true,
                       direction = Grid.Direction.Column,
                       justify = Grid.Justify.SpaceBetween,
                       spacing = 0,
                       style = Styles.overlayActionsContainer.inline)(
                    Grid(item = true)(
                      Grid(container = true, spacing = 0, justify = Grid.Justify.SpaceBetween)(
                        Grid(item = true)(SmileButton(onValueChanged = handleLikeValueChanged)()),
                        Grid(item = true)(CryButton(onValueChanged = handleLikeValueChanged)())
                      )),
                    Grid(item = true)(
                      Grid(container = true, spacing = 0, justify = Grid.Justify.SpaceBetween)(
                        Grid(item = true)(LaughButton(onValueChanged = handleLaughValueChanged)()),
                        Grid(item = true)(FrownButton(onValueChanged = handleLaughValueChanged)())
                      ))
                  )
                )
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

  def apply(routerCtl: DoteRouterCtl,
            dotable: Dotable,
            elevation: Int = 8,
            width: String = "175px") =
    component.withProps(Props(routerCtl, dotable, elevation, width))
}
