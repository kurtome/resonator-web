package kurtome.dote.web.components.widgets.detail

import dote.proto.api.dotable.Dotable
import japgolly.scalajs.react._
import japgolly.scalajs.react.extra.router.RouterCtl
import japgolly.scalajs.react.vdom.html_<^._
import kurtome.dote.web.DoteRoutes.{DoteRoute, PodcastEpisodeRoute}
import kurtome.dote.web.SharedStyles
import kurtome.dote.web.components.ComponentHelpers._
import kurtome.dote.web.components.materialui._
import kurtome.dote.web.components.widgets.SiteLink

import scala.scalajs.js

object EpisodeTable {

  case class Props(routerCtl: RouterCtl[DoteRoute], dotable: Dotable)

  val rowsPerPageOptions = Array(5, 10, 20)
  case class State(page: Int = 0, rowsPerPage: Int = rowsPerPageOptions(0))

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

      Paper(className = SharedStyles.episodeTableContainer)(
        Table()(
          TableHead()(
            TableRow(key = Some(p.dotable.id + "header"))(
              TableCell()("Episodes"),
            )
          ),
          TableBody()(
            (episodePage.zipWithIndex map {
              case (episode, i) =>
                val id = episode.id
                val key: String = if (id.isEmpty) i.toString else id
                val slug = episode.slug
                val detailRoute = PodcastEpisodeRoute(id = id, slug = slug)
                val durationInfo = durationSecToMin(episode.getDetails.getPodcastEpisode.durationSec)
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
                    Typography(typographyType = Typography.Type.Body1)(SiteLink(p.routerCtl, detailRoute)(episode.getCommon.title)),
                    Typography(typographyType = Typography.Type.Body2)(summaryInfo)
                  )
                )
            }).toVdomArray
          ),
          TableFooter()(
            TableRow(key = Some(p.dotable.id + "footer"))(
              TablePagination(
                rowsPerPage = s.rowsPerPage,
                count = episodes.size,
                page = s.page,
                rowsPerPageOptions = rowsPerPageOptions,
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
