package kurtome.dote.web.components.widgets.feed

import japgolly.scalajs.react.BackendScope
import japgolly.scalajs.react._
import japgolly.scalajs.react.vdom.Attr.Ref
import japgolly.scalajs.react.vdom.VdomElement
import japgolly.scalajs.react.vdom.html_<^._
import kurtome.dote.proto.api.dotable.Dotable
import kurtome.dote.proto.api.dote.Dote
import kurtome.dote.shared.util.observer.SimpleObservable
import kurtome.dote.web.CssSettings._
import kurtome.dote.web.audio.AudioPlayer
import kurtome.dote.web.components.ComponentHelpers._
import kurtome.dote.web.components.lib.LazyLoad
import kurtome.dote.web.components.materialui.Collapse
import kurtome.dote.web.components.materialui.Divider
import kurtome.dote.web.components.materialui.Grid
import kurtome.dote.web.components.materialui.GridContainer
import kurtome.dote.web.components.materialui.GridItem
import kurtome.dote.web.components.materialui.Hidden
import kurtome.dote.web.components.materialui.IconButton
import kurtome.dote.web.components.materialui.Icons
import kurtome.dote.web.components.materialui.Paper
import kurtome.dote.web.components.materialui.Typography
import kurtome.dote.web.components.widgets.button.ShareButton
import kurtome.dote.web.components.widgets.button.emote.DoteEmoteButton
import kurtome.dote.web.components.widgets.card.CardActionShim
import kurtome.dote.web.constants.MuiTheme
import kurtome.dote.web.utils.BaseBackend
import kurtome.dote.web.utils.IsMobile
import kurtome.dote.web.utils.LoggedInPersonManager
import kurtome.dote.web.utils.ScrollManager
import org.scalajs.dom
import org.scalajs.dom.ClientRect
import org.scalajs.dom.html
import wvlet.log.LogSupport

import scala.language.postfixOps

object DotableActionsCardWrapper extends LogSupport {

  object Styles extends StyleSheet.Inline {

    import dsl._

    val cardHeader = style(
      backgroundColor :=! MuiTheme.theme.palette.extras.cardHeader
    )

    val moreFooter = style(
      backgroundColor :=! MuiTheme.theme.palette.extras.cardHeader,
      width(100 %%)
    )

    val moreContainer = style(
      zIndex(2),
      position.absolute,
      width(100 %%),
      height(100 %%)
    )

    val default = style(
      backgroundColor :=! MuiTheme.theme.palette.background.paper
    )

    val accent = style(
      backgroundColor :=! MuiTheme.theme.palette.extras.accentPaperBackground
    )

    val actionsCollapseContainer = style(
      pointerEvents := "auto"
    )

  }

  object Variants extends Enumeration {
    val Accent = Value // recent activity feed
    val CardHeader = Value // recent activity feed
    val Default = Value
  }
  type Variant = Variants.Value

  case class Props(dotable: Dotable, elevation: Int, variant: Variant, alwaysExpanded: Boolean)
  case class State(dotable: Dotable,
                   hover: Boolean = false,
                   expanded: Boolean = false,
                   hasHovered: Boolean = false,
                   boundingClientRect: Option[ClientRect] = None)

  class Backend(bs: BackendScope[Props, State]) extends BaseBackend(Styles) {

    def paperStyle(p: Props): StyleA = {
      p.variant match {
        case Variants.Accent => Styles.accent
        case Variants.CardHeader => Styles.cardHeader
        case _ => Styles.default
      }
    }

    var expandTimer: Option[Int] = None
    var collapseTimer: Option[Int] = None

    val onMouseEnter = bs.props map { p =>
      if (!p.alwaysExpanded) {
        collapseTimer.foreach(id => dom.window.clearTimeout(id))
        expandTimer = Some(
          dom.window.setTimeout(() => bs.modState(_.copy(expanded = true)).runNow(),
                                timeout = 1500))
        bs.modState(_.copy(hover = true, hasHovered = true)).runNow()
      }
    }

    val onMouseLeave = bs.props map { p =>
      if (!p.alwaysExpanded) {
        expandTimer.foreach(id => dom.window.clearTimeout(id))
        collapseTimer = Some(
          dom.window.setTimeout(() => bs.modState(_.copy(expanded = false)).runNow(),
                                timeout = 1000))
        bs.modState(_.copy(hover = false)).runNow()
      }
    }

    val handleUnmount = Callback {
      expandTimer.foreach(id => dom.window.clearTimeout(id))
      collapseTimer.foreach(id => dom.window.clearTimeout(id))
    }

    private var outerRef: Option[html.Element] = None

    // on mobile, treat centered as if it hovered
    if (IsMobile.value) {
      ScrollManager.stateObservable addObserver { _ =>
        // bs.modState(_.copy(boundingClientRect = outerRef.map(_.getBoundingClientRect()))).runNow()
        outerRef.foreach(element => {
          val rect = element.getBoundingClientRect()
          val centerY = dom.window.innerHeight / 2
          if (rect.top < centerY && rect.bottom > centerY) {
            onMouseEnter.runNow()
          } else if (bs.state.runNow().hover) {
            onMouseLeave.runNow()
          }
        })
      }
    }

    def handleDoteChanged(dote: Dote) =
      bs.modState(s => s.copy(dotable = s.dotable.withDote(dote)))

    def render(p: Props, s: State, pc: PropsChildren): VdomElement = {
      val dotable = s.dotable

      <.div(
        ^.position.relative,
        ^.onMouseEnter --> onMouseEnter,
        ^.onMouseLeave --> onMouseLeave,
        Paper(style = paperStyle(p), elevation = if (s.hover) p.elevation + 2 else p.elevation)(
//          CardActionShim(dotable,
//                         active = s.hover && !s.expanded,
//                         onDoteChanged = handleDoteChanged)(),
          pc,
          // hide the menu until as late a possible to avoid rendering it on every card on the page
          Hidden(xsUp = !s.hasHovered)(
            Collapse(in = s.expanded, style = Styles.actionsCollapseContainer)(
              Divider()(),
              GridContainer()(
                GridItem(xs = 12,
                         hidden = Grid.HiddenProps(xsUp = LoggedInPersonManager.isNotLoggedIn))(
                  GridContainer(justify = Grid.Justify.Center)(
                    GridItem()(DoteEmoteButton(dotable,
                                               showAllOptions = true,
                                               onDoteChanged = handleDoteChanged)())
                  )
                ),
                GridItem(xs = 12,
                         hidden = Grid.HiddenProps(xsUp = LoggedInPersonManager.isNotLoggedIn))(
                  Divider()()),
                GridItem(xs = 12)(
                  GridContainer(justify = Grid.Justify.Center,
                                alignItems = Grid.AlignItems.Center)(
                    GridItem()(Typography()("Share")),
                    GridItem()(ShareButton(dotableUrl(dotable))())
                  )
                ),
                GridItem(xs = 12, hidden = Grid.HiddenProps(xsUp = !AudioPlayer.canPlay(dotable)))(
                  Divider()()
                ),
                GridItem(xs = 12, hidden = Grid.HiddenProps(xsUp = !AudioPlayer.canPlay(dotable)))(
                  GridContainer(justify = Grid.Justify.Center,
                                alignItems = Grid.AlignItems.Center)(
                    GridItem()(Typography()("Play")),
                    GridItem()(
                      IconButton(onClick = Callback(AudioPlayer.startPlayingEpisode(dotable)),
                                 color = IconButton.Colors.Primary)(Icons.PlayArrow()))
                  )
                )
              )
            ))
        )
      ).ref(el => outerRef = Some(el))
    }
  }

  val component = ScalaComponent
    .builder[Props](this.getClass.getSimpleName)
    .initialStateFromProps(p =>
      State(p.dotable, expanded = p.alwaysExpanded, hasHovered = p.alwaysExpanded))
    .backend(new Backend(_))
    .renderPCS((builder, p, pc, s) => builder.backend.render(p, s, pc))
    .componentWillUnmount(x => x.backend.handleUnmount)
    .build

  def apply(dotable: Dotable,
            elevation: Int = 0,
            variant: Variant = Variants.Default,
            alwaysExpanded: Boolean = false) =
    component.withProps(Props(dotable, elevation, variant, alwaysExpanded))

}
