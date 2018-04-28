package kurtome.dote.web.components.views

import japgolly.scalajs.react._
import japgolly.scalajs.react.vdom.html_<^._
import kurtome.dote.proto.api.action.search.SearchResponse
import kurtome.dote.proto.api.dotable.Dotable
import kurtome.dote.shared.constants.QueryParamKeys
import kurtome.dote.web.DoteRoutes._
import kurtome.dote.web.SharedStyles
import kurtome.dote.web.components.ComponentHelpers._
import kurtome.dote.web.CssSettings._
import kurtome.dote.web.DoteRoutes.AddRoute
import kurtome.dote.web.components.materialui._
import kurtome.dote.web.components.widgets.CompactItemList
import kurtome.dote.web.components.widgets.Fader
import kurtome.dote.web.components.widgets.MainContentSection
import kurtome.dote.web.components.widgets.SearchBox
import kurtome.dote.web.components.widgets.SiteLink
import kurtome.dote.web.components.widgets.card.EpisodeCard
import kurtome.dote.web.components.widgets.card.PodcastCard
import kurtome.dote.web.constants.MuiTheme
import kurtome.dote.web.utils.BaseBackend
import org.scalajs.dom
import wvlet.log.LogSupport
import scalacss.internal.mutable.StyleSheet

import scala.scalajs.js

object SearchView extends LogSupport {

  object Styles extends StyleSheet.Inline {
    import dsl._

    val announcementText = style(
      marginTop(SharedStyles.spacingUnit),
      fontSize(1.2 rem)
    )

    val paperContainer = style(
      padding(SharedStyles.spacingUnit)
    )
  }

  case class Props(query: String)
  case class State(query: String = "",
                   showAddText: Boolean = false,
                   combinedResults: Seq[Dotable] = Nil,
                   podcasts: Seq[Dotable] = Nil,
                   episodes: Seq[Dotable] = Nil)

  class Backend(bs: BackendScope[Props, State]) extends BaseBackend(Styles) {

    def handleNewProps(newProps: Props) = {
      debug(s"new Props $newProps")
      bs.modState(_.copy(query = newProps.query))
    }

    def updateUrlForQuery(query: String) = {
      val url = doteRouterCtl
        .urlFor(SearchRoute(Map(QueryParamKeys.query -> query).filter(_._2.nonEmpty)))
        .value
      dom.window.history.pushState(new js.Object(), "Resonator", url)
    }

    def handleSearchResultsUpdated(response: SearchResponse) = Callback {
      val podcasts =
        response.resultsByKind
          .find(_.kind == Dotable.Kind.PODCAST)
          .map(_.dotables)
          .getOrElse(Nil)

      val episodes =
        response.resultsByKind
          .find(_.kind == Dotable.Kind.PODCAST_EPISODE)
          .map(_.dotables)
          .getOrElse(Nil)
      bs.modState(
          _.copy(query = response.query,
                 showAddText = response.query.nonEmpty,
                 combinedResults = response.combinedResults,
                 podcasts = podcasts,
                 episodes = episodes))
        .runNow()
      updateUrlForQuery(response.query)
    }

    def render(p: Props, s: State): VdomElement = {
      MainContentSection()(
        GridContainer(justify = Grid.Justify.Center)(
          GridItem(xs = 12, sm = 10, md = 8)(
            Paper(style = Styles.paperContainer)(
              SearchBox(s.query, onResultsUpdated = handleSearchResultsUpdated)()
            ),
            Fader(in = s.showAddText)(
              Typography(variant = Typography.Variants.Body1, style = Styles.announcementText)(
                "Not the podcasts you're looking for? Add a podcast to the site ",
                SiteLink(AddRoute)("here"),
                "."
              )
            )
          ),
          GridItem(xs = 12, hidden = Grid.HiddenProps(xsUp = s.combinedResults.isEmpty))(
            GridContainer()(
              GridItem(xs = 12)(
                Typography(variant = Typography.Variants.SubHeading)("Combined Results")),
              (s.combinedResults.zipWithIndex map {
                case (dotable, i) =>
                  <.div(
                    ^.width := "100%",
                    ^.marginBottom := "16px",
                    renderCard(dotable)
                  )
              }).toVdomArray
            )
          )
        ))
    }

    private def renderCard(dotable: Dotable): VdomNode = {
      dotable.kind match {
        case Dotable.Kind.PODCAST =>
          PodcastCard(dotable, variant = PodcastCard.Variants.Activity, showDescription = true)()
        case _ =>
          EpisodeCard(dotable, variant = EpisodeCard.Variants.Activity, showDescription = true)()
      }
    }
  }

  val component = ScalaComponent
    .builder[Props](this.getClass.getSimpleName)
    .initialState(State())
    .backend(new Backend(_))
    .renderPS((b, p, s) => b.backend.render(p, s))
    .componentWillMount(x => x.backend.handleNewProps(x.props))
    .componentWillReceiveProps(x => x.backend.handleNewProps(x.nextProps))
    .build

  def apply(route: SearchRoute) = {
    val query = route.queryParams.getOrElse(QueryParamKeys.query, "")
    component.withProps(Props(query))
  }
}
