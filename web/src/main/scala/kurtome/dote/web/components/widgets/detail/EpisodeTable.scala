package kurtome.dote.web.components.widgets.detail

import kurtome.dote.proto.api.dotable.Dotable
import japgolly.scalajs.react._
import japgolly.scalajs.react.extra.router.RouterCtl
import japgolly.scalajs.react.vdom.html_<^._
import kurtome.dote.web.CssSettings._
import kurtome.dote.web.DoteRoutes.{DoteRoute, DetailsRoute}
import kurtome.dote.web.SharedStyles
import kurtome.dote.web.components.ComponentHelpers
import kurtome.dote.web.components.ComponentHelpers._
import kurtome.dote.web.components.materialui._
import kurtome.dote.web.components.widgets.SiteLink
import kurtome.dote.web.utils.BaseBackend

import scala.scalajs.js
import scalacss.internal.mutable.StyleSheet

object EpisodeTable {

  object Styles extends StyleSheet.Inline {
    import dsl._

    val episodesHeader = style(
      marginTop(SharedStyles.spacingUnit * 2),
      marginLeft(SharedStyles.spacingUnit * 2),
      marginBottom(SharedStyles.spacingUnit * 2)
    )

    val pageNumbersLabel = style(
      marginRight(SharedStyles.spacingUnit * 2)
    )

    val episodeTableContainer = style(
      padding(0 px)
    )

    val tableCell = style(
      padding(SharedStyles.spacingUnit * 2)
    )

    val truncateText = style(
      whiteSpace.nowrap,
      overflow.hidden,
      textOverflow := "ellipsis"
    )

  }

  case class Props(dotable: Dotable)

  case class State(page: Int = 0, rowsPerPage: Int = 10)

  private def lastPage(p: Props, s: State): Int = {
    val numEpisodes = p.dotable.getRelatives.children.size
    numEpisodes / s.rowsPerPage
  }

  private def episodesByRecency(dotable: Dotable) = {
    dotable.kind match {
      case Dotable.Kind.PODCAST => dotable.getRelatives.children
      case _ => Nil
    }
  }

  class Backend(bs: BackendScope[Props, State]) extends BaseBackend(Styles) {

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
      bs.modState(_.copy(page = Math.min(lastPage(p, s), s.page + 1)))

    def render(p: Props, s: State): VdomElement = {
      val episodes = episodesByRecency(p.dotable)

      val pageStartIndex = s.rowsPerPage * s.page
      val pageEndIndex = Math.min(pageStartIndex + s.rowsPerPage, episodes.size)
      val episodesOnPage = episodes.slice(pageStartIndex, pageEndIndex)

      // Fill the page with blank episodes if there is extra space
      val episodePage = episodesOnPage ++
        (1 to s.rowsPerPage - episodesOnPage.size).map(_ => Dotable.defaultInstance)

      val isXs = isBreakpointXs

      Grid(container = true, spacing = 0)(
        Grid(item = true, xs = 12)(
          Paper(style = Styles.episodeTableContainer)(
            Table()(
              TableHead()(
                TableRow()(TableCell(style = Styles.tableCell)(
                  Typography(variant = Typography.Variants.SubHeading)("Episodes")))
              ),
              TableBody()(
                (episodePage.zipWithIndex map {
                  case (episode, i) =>
                    val id = episode.id
                    val key: String = if (id.isEmpty) i.toString else id
                    val slug = episode.slug
                    val detailRoute = DetailsRoute(id = id, slug = slug)
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
                      TableCell(style = Styles.tableCell)(
                        <.div(
                          ^.position := "relative",
                          ^.height := "2.5em",
                          <.div(
                            ^.position := "absolute",
                            ^.maxWidth := "100%",
                            Typography(variant = Typography.Variants.Body1,
                                       style = Styles.truncateText)(
                              SiteLink(detailRoute)(episode.getCommon.title)),
                            Typography(variant = Typography.Variants.Caption,
                                       style = Styles.truncateText)(summaryInfo)
                          )
                        )
                      )
                    )
                }).toVdomArray
              ),
              TableFooter()(
                TableRow()(
                  TableCell(style = Styles.tableCell)(
                    Grid(
                      container = true,
                      spacing = 0,
                      justify = if (isXs) Grid.Justify.SpaceBetween else Grid.Justify.FlexEnd,
                      alignItems = Grid.AlignItems.Center
                    )(
                      Grid(item = true)(
                        Typography(variant = Typography.Variants.Caption,
                                   style = Styles.pageNumbersLabel)(
                          s"${pageStartIndex + 1}-$pageEndIndex of ${episodes.size}")
                      ),
                      Grid(item = true)(
                        <.span(
                          IconButton(disabled = s.page <= 0, onClick = handlePrevPageClicked(s))(
                            Icons.KeyboardArrowLeft()),
                          IconButton(
                            disabled = s.page >= lastPage(p, s),
                            onClick = handleNextPageClicked(p, s))(Icons.KeyboardArrowRight())
                        )
                      )
                    )
                  )))
            )
          )
        )
      )

    }
  }

  val component = ScalaComponent
    .builder[Props](this.getClass.getSimpleName)
    .initialState(State(rowsPerPage = if (ComponentHelpers.isBreakpointXs) 5 else 10))
    .backend(new Backend(_))
    .renderPS((builder, props, state) => builder.backend.render(props, state))
    .build

  def apply(p: Props) = component.withProps(p)
}
