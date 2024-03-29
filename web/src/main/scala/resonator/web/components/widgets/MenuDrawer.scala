package resonator.web.components.widgets

import japgolly.scalajs.react._
import japgolly.scalajs.react.vdom.html_<^._
import resonator.web.CssSettings._
import resonator.web.DoteRoutes._
import resonator.web.audio.RadioTuner
import resonator.web.components.ComponentHelpers._
import resonator.web.components.materialui._
import resonator.web.constants.MuiTheme
import resonator.web.utils.BaseBackend
import resonator.web.utils.LoggedInPersonManager
import scalacss.internal.mutable.StyleSheet
import wvlet.log.LogSupport

object MenuDrawer extends LogSupport {

  val drawerWidth = 240

  object Styles extends StyleSheet.Inline {
    import dsl._

    val fixedDrawerPaper = style(
      position.relative,
      height(100 %%),
      width(drawerWidth px),
      backgroundColor :=! MuiTheme.theme.palette.extras.accentPaperBackground
    )

    val temporaryDrawerPaper = style(
      width(drawerWidth px),
      backgroundColor :=! MuiTheme.theme.palette.extras.accentPaperBackground
    )

  }

  case class Props(open: Boolean, onClose: Callback)
  case class State()

  class Backend(bs: BackendScope[Props, State]) extends BaseBackend(Styles) {

    private def routeClicked(p: Props, route: => DoteRoute): Callback = {
      doteRouterCtl.set(route) >> p.onClose
    }

    private def renderDrawerContents(p: Props): VdomNode = {
      <.div(
        ^.width := asPxStr(drawerWidth),
        ^.position.fixed,
        Toolbar(disableGutters = false)(
          SiteTitle(color = SiteTitle.Colors.Dark)()
        ),
        Divider()(),
        List()(
          Hidden(xsUp = LoggedInPersonManager.isLoggedIn)(
            ListItem(button = true, onClick = routeClicked(p, LoginRoute()))(
              ListItemText()("Login")
            ),
            ListItem(button = true, onClick = routeClicked(p, LoginRoute()))(
              ListItemText()("Sign Up for Free")
            ),
            Divider()()
          ),
          ListItem(button = true, onClick = routeClicked(p, HomeRoute()))(
            ListItemIcon()(Icons.Home()),
            ListItemText()("Home")
          ),
          ListItem(button = true, onClick = routeClicked(p, SearchRoute()))(
            ListItemIcon()(Icons.Search()),
            ListItemText()("Search")
          ),
          ListItem(button = true, onClick = routeClicked(p, RadioTuner.curRoute))(
            ListItemIcon()(Icons.Radio()),
            ListItemText()("Radio")
          ),
          Hidden(xsUp = LoggedInPersonManager.isNotLoggedIn)(
            ListItem(button = true,
                     onClick =
                       routeClicked(p, ProfileRoute(username = LoggedInPersonManager.username)))(
              ListItemIcon()(Icons.AccountCircle()),
              ListItemText(secondary = LoggedInPersonManager.username)("Profile")
            )
          )
        )
      )
    }

    def render(p: Props, s: State): VdomElement = {
      ReactFragment(
        Hidden(smDown = true)(
          Drawer(
            PaperProps = Paper.Props(style = Styles.fixedDrawerPaper),
            variant = Drawer.Variants.Permanent,
            anchor = Drawer.Anchors.Left
          )(
            renderDrawerContents(p)
          )
        ),
        Hidden(mdUp = true)(
          Drawer(
            PaperProps = Paper.Props(style = Styles.temporaryDrawerPaper),
            variant = Drawer.Variants.Temporary,
            open = p.open,
            onClose = p.onClose,
            anchor = Drawer.Anchors.Left
          )(
            renderDrawerContents(p)
          )
        )
      )
    }
  }

  val component = ScalaComponent
    .builder[Props](this.getClass.getSimpleName)
    .initialState(State())
    .backend(new Backend(_))
    .renderPS((b, p, s) => b.backend.render(p, s))
    .build

  def apply(open: Boolean = false, onClose: Callback = Callback.empty)(c: CtorType.ChildArg*) =
    component(Props(open, onClose))
}
