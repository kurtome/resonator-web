package kurtome.dote.web.components.widgets.feed

import japgolly.scalajs.react.BackendScope
import japgolly.scalajs.react._
import japgolly.scalajs.react.vdom.VdomElement
import japgolly.scalajs.react.vdom.html_<^._
import kurtome.dote.proto.api.activity.Activity
import kurtome.dote.proto.api.dotable.Dotable
import kurtome.dote.proto.api.feed.FeedActivityList
import kurtome.dote.proto.api.feed.FeedItem
import kurtome.dote.web.CssSettings._
import kurtome.dote.web.audio.AudioPlayer
import kurtome.dote.web.components.ComponentHelpers._
import kurtome.dote.web.components.materialui.Collapse
import kurtome.dote.web.components.materialui.Divider
import kurtome.dote.web.components.materialui.Fade
import kurtome.dote.web.components.materialui.Grid
import kurtome.dote.web.components.materialui.GridContainer
import kurtome.dote.web.components.materialui.GridItem
import kurtome.dote.web.components.materialui.Hidden
import kurtome.dote.web.components.materialui.IconButton
import kurtome.dote.web.components.materialui.Icons
import kurtome.dote.web.components.materialui.Paper
import kurtome.dote.web.components.materialui.Typography
import kurtome.dote.web.components.widgets._
import kurtome.dote.web.components.widgets.button.ShareButton
import kurtome.dote.web.components.widgets.button.emote.DoteEmoteButton
import kurtome.dote.web.components.widgets.card.ActivityCard
import kurtome.dote.web.components.widgets.card.CardActionShim
import kurtome.dote.web.constants.MuiTheme
import kurtome.dote.web.utils.BaseBackend
import kurtome.dote.web.utils.FeedIdRoutes
import org.scalajs.dom
import scalacss.ScalaCssReact._
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
  case class State(hover: Boolean = false, expanded: Boolean = false)

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
                                timeout = 2000))
        bs.modState(_.copy(hover = true)).runNow()
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

    def render(p: Props, s: State, pc: PropsChildren): VdomElement = {
      <.div(
        ^.position.relative,
        ^.onMouseEnter --> onMouseEnter,
        ^.onMouseLeave --> onMouseLeave,
        Paper(style = paperStyle(p), elevation = if (s.hover) p.elevation + 2 else p.elevation)(
          CardActionShim(p.dotable, s.hover && !s.expanded)(),
          pc,
          Collapse(in = s.expanded, style = Styles.actionsCollapseContainer)(
            Divider()(),
            GridContainer()(
              GridItem(xs = 12)(
                GridContainer(justify = Grid.Justify.Center)(
                  GridItem()(DoteEmoteButton(p.dotable, showAllOptions = true)())
                )
              ),
              GridItem(xs = 12)(Divider()()),
              GridItem(xs = 12)(
                GridContainer(justify = Grid.Justify.Center, alignItems = Grid.AlignItems.Center)(
                  GridItem()(Typography()("Share")),
                  GridItem()(ShareButton(dotableUrl(p.dotable))())
                )
              ),
              GridItem(xs = 12, hidden = Grid.HiddenProps(xsUp = !AudioPlayer.canPlay(p.dotable)))(
                Divider()()
              ),
              GridItem(xs = 12, hidden = Grid.HiddenProps(xsUp = !AudioPlayer.canPlay(p.dotable)))(
                GridContainer(justify = Grid.Justify.Center, alignItems = Grid.AlignItems.Center)(
                  GridItem()(Typography()("Play")),
                  GridItem()(
                    IconButton(onClick = Callback(AudioPlayer.startPlayingEpisode(p.dotable)),
                               color = IconButton.Colors.Primary)(Icons.PlayArrow()))
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
    .initialStateFromProps(p => State(expanded = p.alwaysExpanded))
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
