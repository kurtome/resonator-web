package kurtome.dote.web.components.widgets.detail

import kurtome.dote.proto.api.dotable.Dotable
import kurtome.dote.proto.db.dotable.ExternalUrls
import japgolly.scalajs.react._
import japgolly.scalajs.react.extra.router.RouterCtl
import japgolly.scalajs.react.vdom.html_<^._
import kurtome.dote.web.CssSettings._
import kurtome.dote.web.DoteRoutes.DoteRoute
import kurtome.dote.web.SharedStyles
import kurtome.dote.web.components.ComponentHelpers._
import kurtome.dote.web.components.lib.LazyLoad
import kurtome.dote.web.components.materialui._
import kurtome.dote.web.components.widgets.detail.DetailFieldList.{DetailField, LinkFieldValue, TextFieldValue}
import kurtome.dote.web.components.widgets.{ContentFrame, PodcastTile}
import kurtome.dote.web.utils.{Debounce, MuiInlineStyleSheet}
import org.scalajs.dom
import wvlet.log.LogSupport

import scala.scalajs.js
import scalacss.internal.mutable.StyleSheet

object PodcastDetails {

  private object Styles extends StyleSheet.Inline with MuiInlineStyleSheet {
    import dsl._

    val titleText = style(
      lineHeight(1 em)
    )

    val subTitleText = style(
      marginBottom(SharedStyles.spacingUnit * 2)
    )

    val textSectionDivider = style(
      marginTop(SharedStyles.spacingUnit),
      marginBottom(SharedStyles.spacingUnit)
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

    val normalTileContainer = style(
      marginRight(SharedStyles.spacingUnit * 3),
      marginBottom(SharedStyles.spacingUnit * 2)
    )

    val titleFieldContainer = style(
      textAlign.left,
      display.grid
    )

  }
  Styles.addToDocument()
  import Styles.richStyle

  case class Props(routerCtl: RouterCtl[DoteRoute], dotable: Dotable)
  case class State(availableWidth: Int)

  private case class ExtractedFields(title: String = "",
                                     subtitle: String = "",
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
        val subtitle: String = {
          val creator =
            if (podcastDetails.author.isEmpty) None
            else Some(s"by ${podcastDetails.author}")
          val years = epochSecRangeToYearRange(common.publishedEpochSec, common.updatedEpochSec)
          if (creator.isDefined && years.isDefined) {
            s"${creator.get} (${years.get})"
          } else if (creator.isDefined) {
            creator.get
          } else {
            years.getOrElse("")
          }
        }
        ExtractedFields(
          title = common.title,
          subtitle = subtitle,
          summary = common.description,
          externalUrls = podcastDetails.getExternalUrls,
        )
      case Dotable.Kind.PODCAST_EPISODE =>
        val episodeDetails = dotable.getDetails.getPodcastEpisode
        ExtractedFields(
          title = common.title,
          subtitle = epochSecToDate(common.publishedEpochSec),
          summary = common.description
          //Seq(DetailField("Duration", durationSecToMin(episodeDetails.durationSec)))
        )
      case _ => ExtractedFields()
    }
  }

  class Backend(bs: BackendScope[Props, State]) extends LogSupport {
    val updateTileSize: Callback = {
      bs.modState(_.copy(availableWidth = ContentFrame.innerWidthPx))
    }

    val resizeListener: js.Function1[js.Dynamic, Unit] = Debounce.debounce1(waitMs = 200) {
      (e: js.Dynamic) =>
        updateTileSize.runNow()
    }
    dom.window.addEventListener("resize", resizeListener)

    val handleWillUnmount: Callback = Callback {
      dom.window.removeEventListener("resize", resizeListener)
    }

    def render(p: Props, s: State): VdomElement = {
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
               style = Styles.detailsHeaderContainer.inline)(
            Grid(item = true)(
              <.div(
                ^.width := asPxStr(tileContainerWidth),
                <.div(^.className := tileContainerStyle,
                      PodcastTile(routerCtl = p.routerCtl,
                                 dotable = p.dotable,
                                 width = asPxStr(tileWidth))())
              )
            ),
            Grid(item = true, style = Styles.titleFieldContainer.inline)(
              <.div(
                ^.width := asPxStr(detailsWidth),
                Grid(container = true)(
                  Grid(item = true, xs = 12)(
                    Typography(style = Styles.titleText.inline,
                               typographyType = Typography.Type.Headline)(fields.title),
                    Typography(style = Styles.subTitleText.inline,
                               typographyType = Typography.Type.SubHeading)(fields.subtitle),
                    Typography(typographyType = Typography.Type.Body1,
                               dangerouslySetInnerHTML = linkifyAndSanitize(fields.summary))()
                  ),
                  Grid(item = true, xs = 12)(Divider(style = Styles.textSectionDivider.inline)()),
                  Grid(item = true, xs = 12)(
                    DetailFieldList(detailFields)()
                  )
                )
              )
            )
          )
        ),
        Grid(item = true, xs = 12)(
          LazyLoad(once = true, height = 500)(
            EpisodeTable(EpisodeTable.Props(p.routerCtl, p.dotable))()
          )
        )
      )

    }
  }

  val component = ScalaComponent
    .builder[Props](this.getClass.getSimpleName)
    .initialState(State(availableWidth = ContentFrame.innerWidthPx))
    .backend(new Backend(_))
    .renderPS((builder, props, state) => builder.backend.render(props, state))
    .componentWillUnmount(x => x.backend.handleWillUnmount)
    .build

  def apply(p: Props) = component.withKey(p.dotable.id).withProps(p)
}
