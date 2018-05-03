package kurtome.dote.web.components.widgets.detail

import kurtome.dote.proto.api.dotable.Dotable
import japgolly.scalajs.react._
import japgolly.scalajs.react.vdom.html_<^._
import kurtome.dote.proto.db.dotable.DotableDetails
import kurtome.dote.web.CssSettings._
import kurtome.dote.web.DoteRoutes._
import kurtome.dote.web.SharedStyles
import kurtome.dote.web.components.ComponentHelpers
import kurtome.dote.web.components.ComponentHelpers._
import kurtome.dote.web.components.materialui._
import kurtome.dote.web.components.widgets.DotableLink
import kurtome.dote.web.components.widgets.SiteLink
import kurtome.dote.web.utils.BaseBackend
import org.scalajs.dom

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

    val circularProgressWrapper = style(
      padding(SharedStyles.spacingUnit * 2)
    )
  }

  case class Props(dotable: Dotable, initialPage: Int = 0, rowsPerPage: Int = 5)
  case class State(page: Int)

  private def lastPage(p: Props): Int = {
    val numEpisodes = p.dotable.getRelatives.children.size
    numEpisodes / p.rowsPerPage
  }

  private def episodesByRecency(dotable: Dotable) = {
    dotable.kind match {
      case Dotable.Kind.PODCAST => dotable.getRelatives.children
      case _ => Nil
    }
  }

  class Backend(bs: BackendScope[Props, State]) extends BaseBackend(Styles) {

    def goToPage(page: Int) = {
      val dotable = bs.props.runNow().dotable
      val url = doteRouterCtl
        .urlFor(DetailsRoute(dotable.id, dotable.slug, Map("ep_page" -> page.toString)))
        .value
      dom.window.history.pushState(new js.Object(), "Resonator", url)
    }

    def handlePrevPageClicked(s: State)() = Callback {
      val newPage = Math.max(0, s.page - 1)
      goToPage(newPage)
      bs.modState(_.copy(page = newPage)).runNow()
    }

    def handleNextPageClicked(p: Props, s: State)() = Callback {
      val newPage = Math.min(lastPage(p), s.page + 1)
      goToPage(newPage)
      bs.modState(_.copy(page = newPage)).runNow()
    }

    def render(p: Props, s: State): VdomElement = {
      val episodes = episodesByRecency(p.dotable)

      val pageStartIndex = p.rowsPerPage * s.page
      val pageEndIndex = Math.min(pageStartIndex + p.rowsPerPage, episodes.size)
      val episodesOnPage = episodes.slice(pageStartIndex, pageEndIndex)

      // Fill the page with blank episodes if there is extra space
      val episodePage = episodesOnPage ++
        (1 to p.rowsPerPage - episodesOnPage.size).map(_ => Dotable.defaultInstance)

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
                if (p.dotable.getRelatives.childrenFetched) {
                  renderEpisodeRows(episodePage)
                } else {
                  GridContainer(justify = Grid.Justify.Center)(
                    GridItem(style = Styles.circularProgressWrapper)(
                      CircularProgress(variant = CircularProgress.Variant.Indeterminate)())
                  )
                }
              ),
              TableFooter()(
                TableRow()(
                  TableCell()(
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
                            disabled = s.page >= lastPage(p),
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

    private def renderEpisodeRows(episodePage: Seq[Dotable]) = {
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
                  Typography(variant = Typography.Variants.Body1, noWrap = true)(
                    DotableLink(episode)(episode.getCommon.title)),
                  Typography(variant = Typography.Variants.Caption, noWrap = true)(summaryInfo)
                )
              )
            )
          )
      }).toVdomArray
    }
  }

  val component = ScalaComponent
    .builder[Props](this.getClass.getSimpleName)
    .initialState(State(0))
    .backend(new Backend(_))
    .renderPS((builder, props, state) => builder.backend.render(props, state))
    .componentWillReceiveProps(x => x.modState(_.copy(page = x.nextProps.initialPage)))
    .build

  def apply(p: Props) = component.withProps(p)
}
