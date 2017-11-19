package kurtome.dote.web.components.widgets.detail

import dote.proto.api.dotable.Dotable
import dote.proto.db.dotable.ExternalUrls
import japgolly.scalajs.react._
import japgolly.scalajs.react.extra.router.RouterCtl
import japgolly.scalajs.react.vdom.html_<^._
import kurtome.dote.web.CssSettings._
import kurtome.dote.web.DoteRoutes.DoteRoute
import kurtome.dote.web.InlineStyles
import kurtome.dote.web.components.ComponentHelpers._
import kurtome.dote.web.components.materialui._
import kurtome.dote.web.components.widgets.detail.DetailFieldList.{DetailField, LinkFieldValue, TextFieldValue}
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
      marginBottom(InlineStyles.spacingUnit * 2)
    )

    val textSectionDivider = style(
      marginTop(InlineStyles.spacingUnit),
      marginBottom(InlineStyles.spacingUnit)
    )

  }
  val muiStyles = new MuiInlineStyleSheet(Styles)
  import muiStyles._

  case class Props(routerCtl: RouterCtl[DoteRoute], dotable: Dotable)

  case class State()

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

  class Backend(bs: BackendScope[Props, State]) {

    def render(p: Props, s: State): VdomElement = {
      val fields = extractFields(p.dotable)
      val podcastDetails = p.dotable.getDetails.getPodcast
      val detailFields =
        Seq[DetailFieldList.DetailField](
          DetailField("Website",
                      Seq(LinkFieldValue(podcastDetails.websiteUrl, podcastDetails.websiteUrl))),
          DetailField("Language", Seq(TextFieldValue(podcastDetails.languageDisplay))),
          DetailField("Listen",
                      Seq(LinkFieldValue("iTunes", podcastDetails.getExternalUrls.itunes)))
        ) filter { field =>
          field.values.filter {
            case TextFieldValue(text) => text.nonEmpty
            case LinkFieldValue(text, url) => text.nonEmpty && url.nonEmpty
          }.nonEmpty
        }

      Grid(container = true, spacing = 0, alignItems = Grid.AlignItems.Center)(
        Grid(item = true, xs = 12)(
          Grid(container = true,
               spacing = 24,
               alignItems = Grid.AlignItems.FlexStart,
               className = InlineStyles.detailsHeaderContainer)(
            Grid(item = true, xs = 12, lg = 4)(
              <.div(
                ^.className := InlineStyles.detailsTileContainer,
                EntityTile(
                  EntityTile.Props(routerCtl = p.routerCtl, dotable = p.dotable, size = "250px"))())
            ),
            Grid(item = true, xs = 12, lg = 8, className = InlineStyles.titleFieldContainer)(
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
    .initialState(State())
    .backend(new Backend(_))
    .renderPS((builder, props, state) => builder.backend.render(props, state))
    .build

  def apply(p: Props) = component.withKey(p.dotable.id).withProps(p)
}
