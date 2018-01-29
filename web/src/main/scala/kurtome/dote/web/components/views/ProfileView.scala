package kurtome.dote.web.components.views

import kurtome.dote.proto.api.feed.Feed
import japgolly.scalajs.react._
import japgolly.scalajs.react.extra.router.RouterCtl
import japgolly.scalajs.react.vdom.html_<^._
import kurtome.dote.proto.api.person.Person
import kurtome.dote.web.CssSettings._
import kurtome.dote.web.DoteRoutes._
import kurtome.dote.web.SharedStyles
import kurtome.dote.web.components.materialui._
import kurtome.dote.web.components.ComponentHelpers._
import kurtome.dote.web.components.widgets.ContentFrame
import kurtome.dote.web.utils.LoggedInPersonManager
import org.scalajs.dom
import wvlet.log.LogSupport

import scala.concurrent.ExecutionContext.Implicits.global

object ProfileView extends LogSupport {

  private object Styles extends StyleSheet.Inline {
    import dsl._

    val fieldsContainer = style(
      marginLeft(SharedStyles.spacingUnit),
      marginRight(SharedStyles.spacingUnit)
    )
  }
  Styles.addToDocument()

  case class State(feed: Feed = Feed.defaultInstance, requestInFlight: Boolean = false)

  class Backend(bs: BackendScope[DoteRouterCtl, State]) extends LogSupport {

    val handleLogout = Callback {
      dom.document.location.assign("/logout")
    }

    def render(routerCtl: DoteRouterCtl, s: State): VdomElement = {
      val loggedInPerson = if (LoggedInPersonManager.isLoggedIn) {
        LoggedInPersonManager.person.get
      } else {
        routerCtl.set(HomeRoute).runNow()
        Person.defaultInstance
      }

      ContentFrame(routerCtl)(
        Grid(container = true, justify = Grid.Justify.Center)(
          Grid(item = true, xs = 12)(
            Grid(container = true, justify = Grid.Justify.Center)(
              Grid(item = true)(
                Typography(typographyType = Typography.Type.Headline)("Profile")
              )
            )
          ),
          Grid(item = true, xs = 12)(
            Grid(container = true, justify = Grid.Justify.Center)(
              Grid(item = true, xs = 12, sm = 8, lg = 6)(
                Paper()(
                  <.div(
                    ^.className := Styles.fieldsContainer,
                    TextField(
                      autoFocus = false,
                      fullWidth = true,
                      disabled = true,
                      value = loggedInPerson.username,
                      name = "username",
                      label = Typography()("username")
                    )(),
                    TextField(
                      autoFocus = false,
                      fullWidth = true,
                      disabled = true,
                      value = loggedInPerson.email,
                      inputType = "email",
                      name = "email",
                      label = Typography()("email address")
                    )()
                  ),
                  Grid(container = true, justify = Grid.Justify.FlexEnd)(
                    Grid(item = true)(
                      Button(color = Button.Color.Accent, onClick = handleLogout)("Logoout")
                    )
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
    .builder[DoteRouterCtl](this.getClass.getSimpleName)
    .initialState(State())
    .backend(new Backend(_))
    .render(x => x.backend.render(x.props, x.state))
    .build

  def apply(routerCtl: RouterCtl[DoteRoute]) = component.withProps(routerCtl)
}
