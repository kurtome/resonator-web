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
import kurtome.dote.web.components.widgets.Fader
import kurtome.dote.web.components.widgets.MainContentSection
import kurtome.dote.web.components.widgets.SearchBox
import kurtome.dote.web.components.widgets.SiteLink
import kurtome.dote.web.components.widgets.card.EpisodeCard
import kurtome.dote.web.components.widgets.card.PodcastCard
import kurtome.dote.web.utils.BaseBackend
import org.scalajs.dom
import wvlet.log.LogSupport
import scalacss.internal.mutable.StyleSheet

import scala.scalajs.js

object SearchView extends LogSupport {

  object Styles extends StyleSheet.Inline {
    import dsl._

    val addPodcastText = style(
      marginBottom(SharedStyles.spacingUnit * 2)
    )

  }

  case class Props(query: String)
  case class State(query: String = "",
                   showAddText: Boolean = false,
                   combinedResults: Seq[Dotable] = Nil)

  class Backend(bs: BackendScope[Props, State]) extends BaseBackend(Styles) {

    def handleNewProps(newProps: Props) = {
      bs.modState(_.copy(query = newProps.query))
    }

    def updateUrlForQuery(query: String) = {
      val url = doteRouterCtl
        .urlFor(SearchRoute(Map(QueryParamKeys.query -> query).filter(_._2.nonEmpty)))
        .value
      dom.window.history.pushState(new js.Object(), "Resonator", url)
    }

    def handleSearchResultsUpdated(response: SearchResponse) = Callback {
      bs.modState(
          _.copy(query = response.query,
                 showAddText = response.query.nonEmpty,
                 combinedResults = response.combinedResults))
        .runNow()
      updateUrlForQuery(response.query)
    }

    def render(p: Props, s: State): VdomElement = {
      <.div(
        MainContentSection(variant = MainContentSection.Variants.Light, verticalPaddingPx = 8)(
          GridContainer(justify = Grid.Justify.Center)(
            GridItem(xs = 12, sm = 10, md = 8)(
              SearchBox(s.query, onResultsUpdated = handleSearchResultsUpdated)()
            )
          )
        ),
        MainContentSection(verticalPaddingPx = 4)(
          Fader(in = s.showAddText)(
            Typography(variant = Typography.Variants.Body1, style = Styles.addPodcastText)(
              "Not the podcasts you're looking for? Add a podcast to the site ",
              SiteLink(AddRoute())("here"),
              "."
            )
          ),
          Hidden(xsUp = s.combinedResults.isEmpty)(
            GridItem(xs = 12)(
              GridContainer()(
                (s.combinedResults.zipWithIndex map {
                  case (dotable, i) =>
                    <.div(
                      ^.key := dotable.id,
                      ^.width := "100%",
                      ^.marginBottom := "16px",
                      renderCard(dotable)
                    )
                }).toVdomArray
              )
            )
          )
        )
      )

    }

    private def renderCard(dotable: Dotable): VdomNode = {
      dotable.kind match {
        case Dotable.Kind.PODCAST =>
          PodcastCard(dotable, showDescription = true)()
        case _ =>
          EpisodeCard(dotable, showDescription = true)()
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
