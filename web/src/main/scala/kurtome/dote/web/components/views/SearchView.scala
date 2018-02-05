package kurtome.dote.web.components.views

import japgolly.scalajs.react._
import japgolly.scalajs.react.vdom.html_<^._
import kurtome.dote.proto.api.dotable.Dotable
import kurtome.dote.web.DoteRoutes._
import kurtome.dote.web.SharedStyles
import kurtome.dote.web.components.ComponentHelpers._
import kurtome.dote.web.CssSettings._
import kurtome.dote.web.DoteRoutes.AddRoute
import kurtome.dote.web.components.AddPodcastDialog
import kurtome.dote.web.components.materialui._
import kurtome.dote.web.components.widgets.Fader
import kurtome.dote.web.components.widgets.SearchBox
import kurtome.dote.web.components.widgets.SiteLink
import kurtome.dote.web.utils.BaseBackend
import wvlet.log.LogSupport

import scalacss.internal.mutable.StyleSheet

object SearchView extends LogSupport {

  object Styles extends StyleSheet.Inline {
    import dsl._

    val announcementText = style(
      fontSize(1.2 rem)
    )
  }

  val handleLoginDialogClosed = (p: Props) =>
    Callback {
      if (p.currentRoute == AddRoute) {
        doteRouterCtl.set(SearchRoute).runNow()
      }
  }

  case class Props(currentRoute: DoteRoute)
  case class State(showAddText: Boolean = false)

  class Backend(bs: BackendScope[Props, State]) extends BaseBackend(Styles) {

    val handleSearchResultsUpdated = (query: String, results: Seq[Dotable]) =>
      bs.modState(_.copy(showAddText = query.nonEmpty && results.isEmpty))

    def render(p: Props, s: State): VdomElement = {
      <.div(
        Grid(container = true, spacing = 0, justify = Grid.Justify.Center)(
          Grid(item = true, xs = 12, sm = 10, md = 8)(
            SearchBox(onResultsUpdated = handleSearchResultsUpdated)()
          ),
          Grid(item = true, xs = 12, sm = 10, md = 8)(
            Fader(in = s.showAddText)(
              Typography(variant = Typography.Variants.Body1, style = Styles.announcementText)(
                "Not the podcasts you're looking for? Add a podcast to the site ",
                SiteLink(AddRoute)("here"),
                "."
              )
            )
          )
        ),
        AddPodcastDialog(
          open = p.currentRoute == AddRoute,
          onClose = handleLoginDialogClosed(p)
        )()
      )
    }
  }

  val component = ScalaComponent
    .builder[Props](this.getClass.getSimpleName)
    .initialState(State())
    .backend(new Backend(_))
    .renderPS((b, p, s) => b.backend.render(p, s))
    .build

  def apply(currentRoute: DoteRoute) = {
    component.withProps(Props(currentRoute))
  }
}
