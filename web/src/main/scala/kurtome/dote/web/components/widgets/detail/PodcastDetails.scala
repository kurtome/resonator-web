package kurtome.dote.web.components.widgets.detail

import dote.proto.api.dotable.Dotable
import dote.proto.db.dotable.ExternalUrls
import japgolly.scalajs.react._
import japgolly.scalajs.react.extra.router.RouterCtl
import japgolly.scalajs.react.vdom.html_<^._
import kurtome.dote.web.CssSettings._
import kurtome.dote.web.DoteRoutes.DoteRoute
import kurtome.dote.web.SharedStyles
import kurtome.dote.web.components.ComponentHelpers._
import kurtome.dote.web.components.materialui._
import kurtome.dote.web.components.widgets.detail.DetailFieldList.{
  DetailField,
  LinkFieldValue,
  TextFieldValue
}
import kurtome.dote.web.components.widgets.EntityTile
import kurtome.dote.web.utils.MuiInlineStyleSheet

import scalacss.internal.mutable.StyleSheet

object PodcastDetails {

  private object Styles extends StyleSheet.Inline {
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
      marginBottom(SharedStyles.spacingUnit * 2),
    )

    val centerTileContainer = style(
      marginLeft.auto,
      marginRight.auto,
      marginBottom(SharedStyles.spacingUnit * 2),
      display.table
    )

    val normalTileContainer = style(
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

  class Backend(bs: BackendScope[Props, _]) {

    def render(p: Props): VdomElement = {
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

      val tileContainerStyle = currentBreakpointString match {
        case "xs" => Styles.centerTileContainer
        case "sm" => Styles.centerTileContainer
        case _ => Styles.normalTileContainer
      }

      Grid(container = true, spacing = 0, alignItems = Grid.AlignItems.FlexStart)(
        Grid(item = true, xs = 12)(
          Grid(container = true,
               spacing = 0,
               alignItems = Grid.AlignItems.FlexStart,
               style = Styles.detailsHeaderContainer.inline)(
            Grid(item = true, xs = 12, md = 4)(
              <.div(^.className := tileContainerStyle,
                    EntityTile(routerCtl = p.routerCtl,
                               dotable = p.dotable,
                               width = "250px")())
            ),
            Grid(item = true, xs = 12, md = 8, style = Styles.titleFieldContainer.inline)(
              Typography(style = Styles.titleText.inline,
                         typographyType = Typography.Type.Headline)(fields.title),
              Typography(style = Styles.subTitleText.inline,
                         typographyType = Typography.Type.SubHeading)(fields.subtitle),
              Typography(typographyType = Typography.Type.Body1,
                         dangerouslySetInnerHTML = linkifyAndSanitize(fields.summary))(),
              Divider(style = Styles.textSectionDivider.inline)(),
              DetailFieldList(detailFields)()
            )
          )
        ),
        Grid(item = true, xs = 12)(
          EpisodeTable(EpisodeTable.Props(p.routerCtl, p.dotable))()
        )
      )

    }
  }

  val component = ScalaComponent
    .builder[Props](this.getClass.getSimpleName)
    .backend(new Backend(_))
    .renderP((builder, props) => builder.backend.render(props))
    .build

  def apply(p: Props) = component.withKey(p.dotable.id).withProps(p)
}
