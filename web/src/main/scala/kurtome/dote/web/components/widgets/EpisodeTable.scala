package kurtome.dote.web.components.widgets

import dote.proto.api.dotable.Dotable
import japgolly.scalajs.react.extra.router.RouterCtl
import kurtome.dote.web.DoteRoutes.{DoteRoute, PodcastDetailRoute}
import kurtome.dote.web.InlineStyles
import kurtome.dote.web.components.materialui._
import kurtome.dote.web.utils.Linkify
import japgolly.scalajs.react._
import japgolly.scalajs.react.vdom.html_<^._
import kurtome.dote.web.components.materialui._
import kurtome.dote.web.components.ComponentHelpers._

import scala.scalajs.js

object EpisodeTable {

  case class Props(routerCtl: RouterCtl[DoteRoute], dotable: Dotable)

  case class State(page: Int = 0, rowsPerPage: Int = 10)

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

    def render(p: Props, s: State): VdomElement = {
      val episodes = episodesByRecency(p.dotable)
      val episodesOnPage = episodes.drop(s.rowsPerPage * s.page).take(s.rowsPerPage)
      // Fill the page with blank episodes if there is extra space
      val episodePage = episodesOnPage ++
        (1 to s.rowsPerPage - episodesOnPage.size).map(_ => Dotable.defaultInstance)

      Paper(className = InlineStyles.episodeTableContainer)(
        Table()(
          TableHead()(
            TableCell()("Episode"),
            TableCell()("Duration"),
            TableCell()("Release")
          ),
          TableBody()(
            (episodePage map { episode =>
              val id = episode.id
              val slug = episode.slug
              val detailRoute = PodcastDetailRoute(id = id, slug = slug)

              TableRow()(
                TableCell()(SiteLink(p.routerCtl, detailRoute)(episode.getCommon.title)),
                TableCell()(
                  durationSecToMin(episode.getDetails.getPodcastEpisode.durationSec)
                ),
                TableCell()(epochSecToDate(episode.getCommon.publishedEpochSec))
              )
            }).toVdomArray
          ),
          TableFooter()(
            TableRow()(
              TablePagination(
                rowsPerPage = s.rowsPerPage,
                count = episodes.size,
                page = s.page,
                rowsPerPageOptions = Array(5, 10, 20),
                onChangePage = onPageChanged,
                onChangeRowsPerPage = onPageSizeChanged
              )()
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
