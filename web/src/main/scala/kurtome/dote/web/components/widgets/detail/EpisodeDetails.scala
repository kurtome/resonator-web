package kurtome.dote.web.components.widgets.detail

import kurtome.dote.proto.api.dotable.Dotable
import japgolly.scalajs.react._
import japgolly.scalajs.react.extra.router.RouterCtl
import japgolly.scalajs.react.vdom.html_<^._
import kurtome.dote.web.CssSettings._
import kurtome.dote.web.DoteRoutes._
import kurtome.dote.web.SharedStyles
import kurtome.dote.web.audio.AudioPlayer
import kurtome.dote.web.components.ComponentHelpers._
import kurtome.dote.web.components.materialui._
import kurtome.dote.web.components.widgets._
import kurtome.dote.web.utils.MuiInlineStyleSheet

import scalacss.internal.mutable.StyleSheet

object EpisodeDetails {

  private object Styles extends StyleSheet.Inline with MuiInlineStyleSheet {
    import dsl._

    val titleText = style(
      lineHeight(1 em)
    )

    val byPodcastText = style(
      marginTop(SharedStyles.spacingUnit)
    )

    val subTitleText = style(
      marginTop(SharedStyles.spacingUnit),
      marginBottom(SharedStyles.spacingUnit * 2)
    )

    val textSectionDivider = style(
      marginTop(SharedStyles.spacingUnit),
      marginBottom(SharedStyles.spacingUnit)
    )

    val headerContainer = style(
      display.flex
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
      marginBottom(SharedStyles.spacingUnit * 2)
    )

    val titleFieldContainer = style(
      textAlign.left,
      display.grid,
      alignContent.center,
      alignItems.center
    )
  }
  Styles.addToDocument()
  import Styles.richStyle

  case class Props(dotable: Dotable)
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
          EpisodeTile(
            dotable = p.dotable,
            width = Math.min(500, ContentFrame.innerWidthPx),
            elevation = 2
          )(),
          Typography(typographyType = Typography.Type.Body1)(
            s"by ",
            SiteLink(DetailsRoute(podcast.id, podcast.slug))(s"${podcast.getCommon.title}"))
        ),
        Grid(item = true, xs = 12)(
          <.div(
            ^.display := (if (AudioPlayer.canPlay(p.dotable)) "block" else "none"),
            Button(onClick = playAudio, raised = true, style = Styles.playButton.inline)(
              "Play",
              Icons.PlayArrow())
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
