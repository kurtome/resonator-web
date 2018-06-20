package resonator.web.components.widgets.detail

import resonator.proto.api.dotable.Dotable
import resonator.proto.db.dotable.ExternalUrls
import japgolly.scalajs.react._
import japgolly.scalajs.react.vdom.html_<^._
import resonator.proto.api.feed.FeedId.TagListId
import resonator.proto.api.tag.Tag
import resonator.web.CssSettings._
import resonator.web.SharedStyles
import resonator.web.DoteRoutes._
import resonator.web.components.ComponentHelpers._
import resonator.web.components.materialui._
import resonator.web.components.widgets.FlatRoundedButton
import resonator.web.components.widgets.SiteLink
import resonator.web.components.widgets.button.ShareButton
import resonator.web.components.widgets.detail.DetailFieldList._
import resonator.web.components.widgets.ContentFrame
import resonator.web.components.widgets.button.emote.DoteEmoteButton
import resonator.web.components.widgets.card.PodcastCard
import resonator.web.components.widgets.feed.DotableActionsCardWrapper
import resonator.web.utils.FeedIdRoutes.TagRouteMapper
import resonator.web.utils.{BaseBackend, Debounce}
import org.scalajs.dom

import scala.scalajs.js
import scalacss.internal.mutable.StyleSheet

object PodcastDetails {

  object Styles extends StyleSheet.Inline {
    import dsl._

    val titleText = style(
      lineHeight(1 em)
    )

    val detailsHeaderContainer = style(
      marginBottom(SharedStyles.spacingUnit * 2)
    )

    val centerTileContainer = style(
      marginLeft.auto,
      marginRight.auto,
      marginBottom(SharedStyles.spacingUnit * 3),
      display.table
    )

    val episodeTableWrapper = style(
      marginTop(SharedStyles.spacingUnit)
    )

    val normalTileContainer = style(
      marginRight(SharedStyles.spacingUnit * 3),
      marginBottom(SharedStyles.spacingUnit * 2)
    )

    val titleFieldContainer = style(
      textAlign.left,
      display.grid
    )

    val divider = style(
      marginTop(8 px),
      marginBottom(8 px)
    )

  }

  case class Props(dotable: Dotable, episodeTablePage: Int)
  case class State(availableWidth: Int = 200)

  private case class ExtractedFields(title: String,
                                     subtitle: VdomElement,
                                     summary: String = "",
                                     author: Option[String] = None,
                                     externalUrls: ExternalUrls = ExternalUrls.defaultInstance)

  private def extractFields(dotable: Dotable): ExtractedFields = {
    val common = dotable.getCommon
    dotable.kind match {
      case Dotable.Kind.PODCAST =>
        val latestEpisode =
          dotable.getRelatives.children.headOption.getOrElse(Dotable.defaultInstance)
        val podcastDetails = dotable.getDetails.getPodcast
        val subtitle: VdomElement = {
          val creator = creatorFromTags(dotable)
          if (creator.isDefined) {
            <.span("from ", renderCreator(creator.get))
          } else {
            <.span("")
          }
        }
        ExtractedFields(
          title = common.title,
          subtitle = subtitle,
          summary = common.description,
          externalUrls = podcastDetails.getExternalUrls
        )
      case Dotable.Kind.PODCAST_EPISODE =>
        val episodeDetails = dotable.getDetails.getPodcastEpisode
        ExtractedFields(
          title = common.title,
          subtitle = <.span(epochSecToDate(common.publishedEpochSec)),
          summary = common.description
        )
      case _ => ExtractedFields("", <.div())
    }
  }

  private def renderTags(dotable: Dotable): VdomElement = {
    val keywords = dotable.getTagCollection.tags
    if (keywords.isEmpty) {
      <.div()
    } else {
      <.div(
        GridContainer(spacing = 8)(
          (keywords map { keyword =>
            GridItem(key = Some(keyword.getId.kind.toString + keyword.getId.key))(
              FlatRoundedButton(
                variant = FlatRoundedButton.Variants.FillLight,
                onClick = doteRouterCtl.set(TagRouteMapper.toRoute(TagListId().withTag(keyword))))(
                keyword.displayValue)
            )
          }) toVdomArray
        )
      )
    }
  }

  private def renderCreator(creator: Tag): VdomElement = {
    SiteLink(TagRouteMapper.toRoute(TagListId().withTag(creator)))(creator.displayValue)
  }

  def creatorFromTags(dotable: Dotable): Option[Tag] = {
    dotable.getTagCollection.tags.find(_.getId.kind == Tag.Kind.PODCAST_CREATOR)
  }

  class Backend(val bs: BackendScope[Props, State]) extends BaseBackend(Styles) {

    val calculateAvailableWidth = (s: State) =>
      Callback {
        if (s.availableWidth != ContentFrame.innerWidthPx) {
          bs.modState(_.copy(availableWidth = ContentFrame.innerWidthPx)).runNow()
        }
    }

    val resizeListener: js.Function1[js.Dynamic, Unit] = Debounce.debounce1(waitMs = 200) {
      (e: js.Dynamic) =>
        calculateAvailableWidth(bs.state.runNow()).runNow()
    }
    dom.window.addEventListener("resize", resizeListener)

    val handleWillUnmount: Callback = Callback {
      dom.window.removeEventListener("resize", resizeListener)
    }

    def render(p: Props, s: State): VdomElement = {
      // Force a re-render if the width needs to be updated
      calculateAvailableWidth(s)

      val fields = extractFields(p.dotable)
      val podcastDetails = p.dotable.getDetails.getPodcast
      val detailFields =
        Seq[DetailFieldList.DetailField](
          DetailField("Website",
                      LinkFieldValue(podcastDetails.websiteUrl, podcastDetails.websiteUrl)),
          DetailField("Language", TextFieldValue(podcastDetails.languageDisplay)),
          DetailField("Listen", LinkFieldValue("iTunes", podcastDetails.getExternalUrls.itunes))
        ) filter { field =>
          field.values exists {
            case TextFieldValue(text) => text.nonEmpty
            case LinkFieldValue(text, url) => text.nonEmpty && url.nonEmpty
          }
        }

      val shouldCenterTile = currentBreakpointString match {
        case "xs" => true
        case _ => false
      }

      val tileContainerStyle = if (shouldCenterTile) {
        Styles.centerTileContainer
      } else {
        Styles.normalTileContainer
      }

      val tileWidth = 250
      val detailsWidth = if (shouldCenterTile) {
        s.availableWidth
      } else {
        s.availableWidth - (tileWidth + 24 + 1)
      }

      val tileContainerWidth = if (shouldCenterTile) {
        s.availableWidth
      } else {
        tileWidth + 24
      }

      GridContainer(spacing = 0, alignItems = Grid.AlignItems.FlexStart)(
        GridItem(xs = 12)(
          GridContainer(spacing = 0,
                        alignItems = Grid.AlignItems.FlexStart,
                        style = Styles.detailsHeaderContainer)(
            GridItem()(
              <.div(
                ^.width := asPxStr(tileContainerWidth),
                <.div(
                  ^.className := tileContainerStyle,
                  <.div(
                    ^.position := "relative",
                    ^.width := asPxStr(tileWidth),
                    DotableActionsCardWrapper(p.dotable, alwaysExpanded = true)(
                      PodcastCard(dotable = p.dotable, variant = PodcastCard.Variants.ImageOnly)())
                  )
                )
              )
            ),
            GridItem(style = Styles.titleFieldContainer)(
              <.div(
                ^.width := asPxStr(detailsWidth),
                GridContainer(spacing = 0)(
                  GridItem(xs = 12)(
                    Typography(style = Styles.titleText, variant = Typography.Variants.SubHeading)(
                      <.b(fields.title)),
                    if (p.dotable.getRelatives.childrenFetched) {
                      Typography(variant = Typography.Variants.Body1)(
                        s"${p.dotable.getRelatives.children.length} Episodes (${epochSecRangeToYearRange(
                          p.dotable.getCommon.publishedEpochSec,
                          p.dotable.getCommon.updatedEpochSec).getOrElse("")})")
                    } else {
                      <.div()
                    },
                    Typography(variant = Typography.Variants.Body1)(fields.subtitle)
                  ),
                  GridItem(xs = 12)(
                    Typography(variant = Typography.Variants.Body1,
                               dangerouslySetInnerHTML = linkifyAndSanitize(fields.summary))()
                  ),
                  GridItem(xs = 12)(Divider(style = Styles.divider)()),
                  GridItem(xs = 12)(
                    DetailFieldList(detailFields)()
                  ),
                  GridItem(xs = 12)(Divider(style = Styles.divider)()),
                  GridItem(xs = 12)(
                    renderTags(p.dotable)
                  )
                )
              )
            )
          )
        ),
        GridItem(xs = 12)(
          <.div(
            ^.className := Styles.episodeTableWrapper,
            EpisodeTable(EpisodeTable.Props(p.dotable, p.episodeTablePage))()
          )
        )
      )

    }
  }

  val component = ScalaComponent
    .builder[Props](this.getClass.getSimpleName)
    .initialState(State())
    .backend(new Backend(_))
    .renderPS((builder, props, state) => builder.backend.render(props, state))
    .componentDidMount(x => x.backend.calculateAvailableWidth(x.state))
    .componentWillUnmount(x => x.backend.handleWillUnmount)
    .build

  def apply(p: Props) = component.withKey(p.dotable.id).withProps(p)
}
