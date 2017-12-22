package kurtome.dote.web.components.widgets.detail

import dote.proto.api.dotable.Dotable
import japgolly.scalajs.react._
import japgolly.scalajs.react.extra.router.RouterCtl
import japgolly.scalajs.react.vdom.html_<^._
import kurtome.dote.web.CssSettings._
import kurtome.dote.web.DoteRoutes.{DoteRoute, PodcastEpisodeRoute}
import kurtome.dote.web.SharedStyles
import kurtome.dote.web.components.ComponentHelpers._
import kurtome.dote.web.components.materialui._
import kurtome.dote.web.components.widgets.SiteLink
import kurtome.dote.web.utils.MuiInlineStyleSheet

import scala.scalajs.js
import scalacss.internal.mutable.StyleSheet

object EpisodeTable {

  private object Styles extends StyleSheet.Inline {
    import dsl._

    val episodesHeader = style(
      marginBottom(SharedStyles.spacingUnit * 2)
    )

    val tableFooterContainer = style(
      padding(SharedStyles.spacingUnit)
    )

    val pageNumbersLabel = style(
      marginRight(SharedStyles.spacingUnit * 2)
    )

  }
  Styles.addToDocument()
  val muiStyles = new MuiInlineStyleSheet(Styles)
  import muiStyles._

  case class Props(routerCtl: RouterCtl[DoteRoute], dotable: Dotable)

  case class State(page: Int = 0, rowsPerPage: Int = 10)

  private def numPages(p: Props, s: State): Int = {
    val numEpisodes = p.dotable.getRelatives.children.size
    (numEpisodes / s.rowsPerPage) + 1
  }

  private def episodesByRecency(dotable: Dotable) = {
    dotable.kind match {
      case Dotable.Kind.PODCAST =>
        dotable.getRelatives.children.sortBy(_.getCommon.publishedEpochSec).reverse
      case _ => Nil
    }
  }

  class Backend(bs: BackendScope[Props, State]) {

    val onPageChanged: (js.Dynamic, Int) => Callback = (event, page) => {
      bs.modState(_.copy(page = page))
    }

    val onPageSizeChanged: (js.Dynamic) => Callback = (event) => {
      bs modState { curState =>
        val rowIndex = curState.page * curState.rowsPerPage
        val newRowsPerPage = event.target.value.asInstanceOf[Int]
        val newPage = rowIndex / newRowsPerPage
        curState.copy(page = newPage, rowsPerPage = newRowsPerPage)
      }
    }

    def handlePrevPageClicked(s: State)() =
      bs.modState(_.copy(page = Math.max(0, s.page - 1)))

    def handleNextPageClicked(p: Props, s: State)() =
      bs.modState(_.copy(page = Math.min(numPages(p, s), s.page + 1)))

    def render(p: Props, s: State): VdomElement = {
      val episodes = episodesByRecency(p.dotable)

      val pageStartIndex = s.rowsPerPage * s.page
      val pageEndIndex = pageStartIndex + s.rowsPerPage
      val episodesOnPage = episodes.slice(pageStartIndex, pageEndIndex)

      // Fill the page with blank episodes if there is extra space
      val episodePage = episodesOnPage ++
        (1 to s.rowsPerPage - episodesOnPage.size).map(_ => Dotable.defaultInstance)

      val isXs = currentBreakpointString == "xs"

      Grid(container = true, spacing = 0)(
        Grid(item = true, xs = 12)(
          Paper(className = SharedStyles.episodeTableContainer)(
            Table()(
              Typography(typographyType = Typography.Type.SubHeading,
                         style = Styles.episodesHeader.inline)("Episodes"),
              Divider()(),
//              TableHead()(
//                TableRow(key = Some(p.dotable.id + "header"))(
//                  TableCell()("Episodes")
//                )
//              ),
              TableBody()(
                (episodePage.zipWithIndex map {
                  case (episode, i) =>
                    val id = episode.id
                    val key: String = if (id.isEmpty) i.toString else id
                    val slug = episode.slug
                    val detailRoute = PodcastEpisodeRoute(id = id, slug = slug)
                    val durationInfo =
                      durationSecToMin(episode.getDetails.getPodcastEpisode.durationSec)
                    val releaseDate = epochSecToDate(episode.getCommon.publishedEpochSec)
                    val summaryInfo =
                      if (durationInfo.nonEmpty && releaseDate.nonEmpty) {
                        s"$durationInfo ($releaseDate)"
                      } else if (durationInfo.nonEmpty) {
                        durationInfo
                      } else {
                        releaseDate
                      }

                    TableRow(key = Some(key))(
                      TableCell()(
                        Typography(typographyType = Typography.Type.Body1)(
                          SiteLink(p.routerCtl, detailRoute)(episode.getCommon.title)),
                        Typography(typographyType = Typography.Type.Body2)(summaryInfo)
                      )
                    )
                }).toVdomArray
              ),
              Divider()(),
              Grid(
                container = true,
                justify = if (isXs) Grid.Justify.SpaceBetween else Grid.Justify.FlexEnd,
                alignItems = Grid.AlignItems.Center,
                style = Styles.tableFooterContainer.inline
              )(
                Grid(item = true)(
                  Typography(typographyType = Typography.Type.Caption,
                             style = Styles.pageNumbersLabel.inline)(
                    s"${pageStartIndex + 1}-$pageEndIndex of ${episodes.size}")
                ),
                Grid(item = true)(
                  <.span(
                    IconButton(disabled = s.page <= 0, onClick = handlePrevPageClicked(s))(
                      Icons.KeyboardArrowLeft()),
                    IconButton(disabled = s.page >= numPages(p, s),
                               onClick = handleNextPageClicked(p, s))(Icons.KeyboardArrowRight())
                  )
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

  def apply(p: Props) = component.withProps(p)
}
