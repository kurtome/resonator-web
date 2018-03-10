package kurtome.dote.web.components.widgets.detail

import kurtome.dote.proto.api.dotable.Dotable
import kurtome.dote.proto.db.dotable.ExternalUrls
import japgolly.scalajs.react._
import japgolly.scalajs.react.vdom.html_<^._
import kurtome.dote.proto.api.tag.Tag
import kurtome.dote.web.CssSettings._
import kurtome.dote.web.SharedStyles
import kurtome.dote.web.DoteRoutes._
import kurtome.dote.web.components.ComponentHelpers._
import kurtome.dote.web.components.ComponentHelpers._
import kurtome.dote.web.components.lib.LazyLoad
import kurtome.dote.web.components.materialui._
import kurtome.dote.web.components.widgets.SiteLink
import kurtome.dote.web.components.widgets.button.ShareButton
import kurtome.dote.web.components.widgets.detail.DetailFieldList._
import kurtome.dote.web.components.widgets.{ContentFrame, PodcastTile}
import kurtome.dote.web.utils.FeedIdRoutes.TagRouteMapper
import kurtome.dote.web.utils.{BaseBackend, Debounce}
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
      marginTop(SharedStyles.spacingUnit),
    )

    val normalTileContainer = style(
      marginRight(SharedStyles.spacingUnit * 3),
      marginBottom(SharedStyles.spacingUnit * 2)
    )

    val titleFieldContainer = style(
      textAlign.left,
      display.grid
    )

  }

  case class Props(dotable: Dotable)
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
          val years = epochSecRangeToYearRange(common.publishedEpochSec, common.updatedEpochSec)
          if (creator.isDefined && years.isDefined) {
            <.span("by ", renderCreator(creator.get), s" (${years.get})")
          } else if (creator.isDefined) {
            <.span("by ", renderCreator(creator.get))
          } else {
            <.span("" + years.getOrElse(""))
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
              Chip(label = Typography()(keyword.displayValue),
                   onClick = doteRouterCtl.set(TagRouteMapper.toRoute(keyword)))()
            )
          }) toVdomArray
        )
      )
    }
  }

  private def renderCreator(creator: Tag): VdomElement = {
    SiteLink(TagRouteMapper.toRoute(creator))(creator.displayValue)
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
            case TextFieldValue(text)      => text.nonEmpty
            case LinkFieldValue(text, url) => text.nonEmpty && url.nonEmpty
          }
        }

      val shouldCenterTile = currentBreakpointString match {
        case "xs" => true
        case "sm" => true
        case _    => false
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

      Grid(container = true, spacing = 0, alignItems = Grid.AlignItems.FlexStart)(
        Grid(item = true, xs = 12)(
          Grid(container = true,
               spacing = 0,
               alignItems = Grid.AlignItems.FlexStart,
               style = Styles.detailsHeaderContainer)(
            Grid(item = true)(
              <.div(
                ^.width := asPxStr(tileContainerWidth),
                <.div(^.className := tileContainerStyle,
                      PodcastTile(dotable = p.dotable, width = asPxStr(tileWidth))())
              )
            ),
            Grid(item = true, style = Styles.titleFieldContainer)(
              <.div(
                ^.width := asPxStr(detailsWidth),
                Grid(container = true)(
                  Grid(item = true, xs = 12)(
                    Typography(style = Styles.titleText, variant = Typography.Variants.Headline)(
                      fields.title),
                    Typography(variant = Typography.Variants.SubHeading)(fields.subtitle)
                  ),
                  GridItem(xs = 12)(
                    GridContainer()(
                      GridItem()(ShareButton()())
                    )
                  ),
                  GridItem(xs = 12)(
                    Typography(variant = Typography.Variants.Body1,
                               dangerouslySetInnerHTML = linkifyAndSanitize(fields.summary))()
                  ),
                  Grid(item = true, xs = 12)(Divider()()),
                  Grid(item = true, xs = 12)(
                    DetailFieldList(detailFields)()
                  ),
                  Grid(item = true, xs = 12)(Divider()()),
                  Grid(item = true, xs = 12)(
                    renderTags(p.dotable)
                  )
                )
              )
            )
          )
        ),
        Grid(item = true, xs = 12)(
          <.div(
            ^.className := Styles.episodeTableWrapper,
            EpisodeTable(EpisodeTable.Props(p.dotable))()
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
