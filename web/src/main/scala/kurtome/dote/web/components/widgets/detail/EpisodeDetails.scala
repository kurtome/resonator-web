package kurtome.dote.web.components.widgets.detail

import dote.proto.api.dotable.Dotable
import japgolly.scalajs.react._
import japgolly.scalajs.react.extra.router.RouterCtl
import japgolly.scalajs.react.vdom.html_<^._
import kurtome.dote.web.CssSettings._
import kurtome.dote.web.DoteRoutes.DoteRoute
import kurtome.dote.web.SharedStyles
import kurtome.dote.web.audio.{AudioPlayer, Howler}
import kurtome.dote.web.audio.Howler.Howl
import kurtome.dote.web.components.ComponentHelpers._
import kurtome.dote.web.components.materialui._
import kurtome.dote.web.components.widgets.EntityTile
import kurtome.dote.web.components.widgets.detail.DetailFieldList._
import kurtome.dote.web.utils.MuiInlineStyleSheet

import scala.scalajs.js
import scalacss.internal.mutable.StyleSheet

object EpisodeDetails {

  private object Styles extends StyleSheet.Inline {
    import dsl._

    val titleText = style(
      lineHeight(1 em)
    )

    val podcastTitleText = style(
      color.rgba(0, 0, 0, 0.5),
      verticalAlign.top,
      display.inline
    )

    val subTitleText = style(
      marginBottom(SharedStyles.spacingUnit * 2)
    )

    val textSectionDivider = style(
      marginTop(SharedStyles.spacingUnit),
      marginBottom(SharedStyles.spacingUnit)
    )

    val headerContainer = style(
      display.flex
    )

    val podcastTileContainer = style(
      display.flex,
      alignItems.flexEnd
    )

    val podcastTile = style(
      float.left,
      marginRight(SharedStyles.spacingUnit * 2),
      marginBottom(SharedStyles.spacingUnit * 2)
    )

    val playButton = style(
      marginTop(SharedStyles.spacingUnit * 2),
      marginBottom(SharedStyles.spacingUnit * 2)
    )

    val detailsHeaderContainer = style(
      marginBottom(SharedStyles.spacingUnit * 2),
    )

    val titleFieldContainer = style(
      textAlign.left,
      display.grid,
      alignContent.center,
      alignItems.center
    )
  }
  Styles.addToDocument()
  val muiStyles = new MuiInlineStyleSheet(Styles)
  import muiStyles._

  case class Props(routerCtl: RouterCtl[DoteRoute], dotable: Dotable)
  case class State()

  class Backend(bs: BackendScope[Props, State]) {

    val playAudio = bs.props map { p =>
      AudioPlayer.startPlayingEpisode(p.dotable)
    }

    def render(p: Props, s: State): VdomElement = {
      val podcast = p.dotable.getRelatives.getParent
      val episodeDetails = p.dotable.getDetails.getPodcastEpisode
      val duration = durationSecToMin(episodeDetails.durationSec)
      val title = p.dotable.getCommon.title
      val description = p.dotable.getCommon.description
      val published = epochSecToDate(p.dotable.getCommon.publishedEpochSec)
      val subtitle = if (duration.nonEmpty && published.nonEmpty) {
        s"$duration ($published)"
      } else if (duration.nonEmpty) {
        published
      } else {
        duration
      }

      Grid(container = true,
           spacing = 0,
           alignItems = Grid.AlignItems.FlexStart,
           className = Styles.detailsHeaderContainer)(
        Grid(item = true, xs = 12)(
          <.div(
            ^.className := Styles.headerContainer,
            <.div(^.className := Styles.podcastTile,
                  EntityTile(routerCtl = p.routerCtl,
                             dotable = p.dotable.getRelatives.getParent,
                             width = "125px")()),
            <.div(
              Typography(style = Styles.titleText.inline,
                         typographyType = Typography.Type.Headline)(title),
              Typography(style = Styles.subTitleText.inline,
                         typographyType = Typography.Type.SubHeading)(subtitle),
              <.div(
                ^.display := (if (AudioPlayer.canPlay(p.dotable)) "block" else "none"),
                Button(onClick = playAudio, raised = true, style = Styles.playButton.inline)(
                  "Play", Icons.PlayArrow())
              )
            )
          )
        ),
        Grid(item = true, xs = 12)(
          Typography(typographyType = Typography.Type.Body1,
                     dangerouslySetInnerHTML = linkifyAndSanitize(description))()
        )
      )

    }
  }

  val component = ScalaComponent
    .builder[Props](this.getClass.getSimpleName)
    .initialState(State())
    .backend(new Backend(_))
    .renderPS((builder, props, state) => builder.backend.render(props, state))
    .build

  def apply(p: Props) = component.withKey(p.dotable.id).withProps(p)
}
