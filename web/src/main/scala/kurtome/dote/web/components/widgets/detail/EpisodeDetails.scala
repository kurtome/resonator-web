package kurtome.dote.web.components.widgets.detail

import kurtome.dote.proto.api.dotable.Dotable
import japgolly.scalajs.react._
import japgolly.scalajs.react.vdom.html_<^._
import kurtome.dote.proto.api.action.set_dote.SetDoteRequest
import kurtome.dote.proto.api.dote.Dote
import kurtome.dote.web.CssSettings._
import kurtome.dote.web.DoteRoutes._
import kurtome.dote.web.SharedStyles
import kurtome.dote.web.audio.AudioPlayer
import kurtome.dote.web.components.ComponentHelpers._
import kurtome.dote.web.components.materialui._
import kurtome.dote.web.components.widgets._
import kurtome.dote.web.components.widgets.button.ShareButton
import kurtome.dote.web.components.widgets.button.emote.DoteEmoteButton
import kurtome.dote.web.components.widgets.card.EpisodeCard
import kurtome.dote.web.rpc.DoteProtoServer
import kurtome.dote.web.utils.BaseBackend
import kurtome.dote.web.utils.Debounce
import kurtome.dote.web.utils.GlobalLoadingManager
import scalacss.internal.mutable.StyleSheet

import scala.scalajs.js

object EpisodeDetails {

  object Styles extends StyleSheet.Inline {
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

    val actionsContainer = style(
      marginTop(SharedStyles.spacingUnit),
      marginBottom(SharedStyles.spacingUnit)
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

  case class Props(dotable: Dotable)
  case class State()

  class Backend(bs: BackendScope[Props, State]) extends BaseBackend(Styles) {

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

      GridContainer(spacing = 0,
                    alignItems = Grid.AlignItems.FlexStart,
                    style = Styles.detailsHeaderContainer)(
        GridItem(xs = 12)(
          <.div(
            ^.position := "relative",
            ^.width := asPxStr(Math.min(500, ContentFrame.innerWidthPx)),
            EpisodeCard(
              dotable = p.dotable,
              elevation = 2,
              disableActions = true
            )()
          ),
          Typography(variant = Typography.Variants.Body1)(
            <.strong(
              s"from ",
              SiteLink(DetailsRoute(podcast.id, podcast.slug))(s"${podcast.getCommon.title}")))
        ),
        GridItem(xs = 12)(
          GridContainer(spacing = 0, style = Styles.actionsContainer)(
            GridItem()(ShareButton()()),
            GridItem()(DoteEmoteButton(p.dotable)()),
            GridItem(hidden = Grid.HiddenProps(xsUp = !AudioPlayer.canPlay(p.dotable)))(
              IconButton(onClick = playAudio, color = IconButton.Colors.Primary)(Icons.PlayArrow())
            )
          )
        ),
        GridItem(xs = 12)(
          Typography(variant = Typography.Variants.Body1,
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
