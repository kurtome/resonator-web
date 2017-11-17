package kurtome.dote.web.components.widgets

import dote.proto.api.dotable.Dotable
import japgolly.scalajs.react._
import japgolly.scalajs.react.extra.router.RouterCtl
import japgolly.scalajs.react.vdom.html_<^._
import kurtome.dote.web.DoteRoutes.DoteRoute
import kurtome.dote.web.InlineStyles
import kurtome.dote.web.components.materialui._
import kurtome.dote.web.components.ComponentHelpers._
import kurtome.dote.web.utils.Linkify
import kurtome.dote.web.CssSettings._
import scalacss.ScalaCssReact._

import scala.scalajs.js
import scala.scalajs.js.JSON
import scalacss.internal.{Css, CssEntry, Renderer, Style}
import scalacss.internal.mutable.StyleSheet

object EntityDetails {

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

    val detailLabel = style(
      marginRight(InlineStyles.spacingUnit)
    )
  }
  Styles.addToDocument()
  private val styleMap: Map[String, js.Dynamic] = styleObjsByClassName(Styles)

  case class Props(routerCtl: RouterCtl[DoteRoute], dotable: Dotable)

  case class State()

  private case class DetailField(label: String, value: String)

  private case class ExtractedFields(title: String = "",
                                     subtitle: String = "",
                                     summary: String = "",
                                     author: Option[String] = None,
                                     details: Seq[DetailField] = Nil)

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
          details = Seq(
            DetailField("Website", podcastDetails.websiteUrl),
            DetailField("Language", podcastDetails.languageDisplay)
          )
        )
      case Dotable.Kind.PODCAST_EPISODE =>
        val episodeDetails = dotable.getDetails.getPodcastEpisode
        ExtractedFields(
          title = common.title,
          subtitle = epochSecToDate(common.publishedEpochSec),
          summary = common.description,
          details = Seq(DetailField("Duration", durationSecToMin(episodeDetails.durationSec)))
        )
      case _ => ExtractedFields()
    }
  }

  class Backend(bs: BackendScope[Props, State]) {

    def render(p: Props, s: State): VdomElement = {
      val fields = extractFields(p.dotable)

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
              Typography(style = styleMap(Styles.titleText),
                         typographyType = Typography.Type.Headline)(fields.title),
              Typography(style = styleMap(Styles.subTitleText),
                         typographyType = Typography.Type.SubHeading)(fields.subtitle),
              Typography(typographyType = Typography.Type.Body1,
                         dangerouslySetInnerHTML = linkifyAndSanitize(fields.summary))(),
              Divider(style = styleMap(Styles.textSectionDivider))(),
              Grid(container = true, spacing = 0, alignItems = Grid.AlignItems.FlexStart)(
                fields.details flatMap { detailField =>
                  val label = <.b(Styles.detailLabel, detailField.label)
                  val content =
                    if (detailField.label == "Website" && Linkify
                          .test(detailField.value, "url")) {
                      <.a(^.href := detailField.value, ^.target := "_blank", detailField.value)
                    } else {
                      <.span(detailField.value)
                    }
                  Seq(
                    Grid(item = true, xs = 2)(
                      Typography(typographyType = Typography.Type.Caption)(label)),
                    Grid(item = true, xs = 10)(
                      Typography(typographyType = Typography.Type.Caption)(content))
                  )
                } toVdomArray
              )
            )
          )
        ),
        Grid(item = true, xs = 12)(),
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

  def apply(p: Props) = component.withProps(p)
}
