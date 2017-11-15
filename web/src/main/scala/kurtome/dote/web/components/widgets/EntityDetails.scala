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

import scala.scalajs.js

object EntityDetails {

  case class Props(routerCtl: RouterCtl[DoteRoute], dotable: Dotable)

  case class State(page: Int = 0, rowsPerPage: Int = 5)

  private case class DetailField(label: String, value: String)

  private case class ExtractedFields(title: String = "",
                                     subtitle: String = "",
                                     summary: String = "",
                                     details: Seq[DetailField])

  private def extractFields(dotable: Dotable): ExtractedFields = {
    val common = dotable.getCommon
    dotable.kind match {
      case Dotable.Kind.PODCAST =>
        val latestEpisode =
          dotable.getRelatives.children.headOption.getOrElse(Dotable.defaultInstance)
        val podcastDetails = dotable.getDetails.getPodcast
        ExtractedFields(
          title = common.title,
          subtitle = "",
          summary = common.description,
          details = Seq(
            DetailField("Creator", podcastDetails.author),
            DetailField("Website", podcastDetails.websiteUrl),
            DetailField("Years",
                        epochSecRangeToYearRange(common.publishedEpochSec,
                                                 common.updatedEpochSec)),
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
      case _ => ExtractedFields("", "", "", Nil)
    }
  }

  private def episodesByRecency(dotable: Dotable) = {
    dotable.kind match {
      case Dotable.Kind.PODCAST => dotable.getRelatives.children.reverse
      case _ => Nil
    }
  }

  class Backend(bs: BackendScope[Props, State]) {

    val onPageChanged: (js.Dynamic, Int) => Callback = (event, page) => {
      bs.modState(_.copy(page = page))
    }

    def render(p: Props, s: State): VdomElement = {
      val fields = extractFields(p.dotable)
      val episodes = episodesByRecency(p.dotable)
      val episodePage = episodes.drop(s.rowsPerPage * s.page).take(s.rowsPerPage)

      Grid(container = true, spacing = 24, alignItems = Grid.AlignItems.Center)(
        Grid(item = true, xs = 12, lg = 4, className = InlineStyles.titleFieldContainer)(
          Typography(typographyType = Typography.Type.Headline)(fields.title),
          Typography(typographyType = Typography.Type.SubHeading)(fields.subtitle)
        ),
        Grid(item = true, xs = 12, lg = 4)(
          <.div(^.className := InlineStyles.centerContainer,
                EntityTile.component(
                  EntityTile.Props(routerCtl = p.routerCtl, dotable = p.dotable, size = "250px")))
        ),
        Grid(item = true, xs = 12, lg = 4, className = InlineStyles.centerTextContainer)(
          Typography(typographyType = Typography.Type.Body1,
                     dangerouslySetInnerHTML = linkifyAndSanitize(fields.summary))()
        ),
        Grid(item = true, xs = 12)(
          Grid(container = true,
               spacing = 24,
               alignItems = Grid.AlignItems.FlexStart,
               className = InlineStyles.detailsFieldContainer)(
            fields.details flatMap { detailField =>
              val label =
                Typography(typographyType = Typography.Type.Body1)(
                  <.b(^.textTransform := "uppercase", detailField.label))
              val content =
                if (detailField.label == "Website" && Linkify.test(detailField.value, "url")) {
                  Typography(typographyType = Typography.Type.Body2)(
                    <.a(^.href := detailField.value, ^.target := "_blank", detailField.value))
                } else {
                  Typography(typographyType = Typography.Type.Body2)(detailField.value)
                }
              Seq(
                Grid(item = true, xs = 4, md = 2)(label),
                Grid(item = true, xs = 8, md = 10)(content)
              )
            } toVdomArray
          )
        ),
        Grid(item = true, xs = 12)(
          Paper(className = InlineStyles.episodeTableContainer)(
            Table()(
              TableHead()(
                TableCell()("Episode"),
                TableCell()("Duration"),
                TableCell()("Release")
              ),
              TableBody()(
                (episodePage map { episode =>
                  TableRow()(
                    TableCell()(episode.getCommon.title),
                    TableCell()(
                      durationSecToMin(episode.getDetails.getPodcastEpisode.durationSec)
                    ),
                    TableCell()(epochSecToDate(episode.getCommon.publishedEpochSec))
                  )
                }).toVdomArray
              ),
              TableFooter()(
                TableRow()(
                  TablePagination(rowsPerPage = s.rowsPerPage,
                                  count = episodes.size,
                                  page = s.page,
                                  rowsPerPageOptions = Array(5, 10, 15),
                                  onChangePage = onPageChanged)()
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
    .initialState(State())
    .backend(new Backend(_))
    .renderPS((builder, props, state) => builder.backend.render(props, state))
    .build
}
