package kurtome.dote.web.components.views

import dote.proto.api.action.add_podcast._
import japgolly.scalajs.react._
import japgolly.scalajs.react.vdom.html_<^._
import kurtome.dote.web.DoteRoutes.DoteRouterCtl
import kurtome.dote.web.Styles
import kurtome.dote.web.rpc.DoteProtoServer
import kurtome.dote.web.components.materialui._
import kurtome.dote.web.components.widgets.{ContentFrame, EntityDetails, EntityTile}
import kurtome.dote.web.components.ComponentHelpers._

import scala.concurrent.ExecutionContext.Implicits.global
import scala.util.{Failure, Success}

object AddPodcastView {

  case class State(request: AddPodcastRequest,
                   response: AddPodcastResponse,
                   requestInFlight: Boolean = false)

  class Backend(bs: BackendScope[DoteRouterCtl, State]) {
    def render(routerCtl: DoteRouterCtl, s: State): VdomElement =
      Grid(container = true, justify = Grid.Justify.Center, spacing = 24)(
        Grid(item = true, xs = 12)(
          Typography(className = Styles.titleText, typographyType = Typography.Type.Display1)(
            "Add Podcast")
        ),
        Grid(item = true, xs = 12)(
          Paper()(
            <.div(
              ^.id := "test",
              ^.className := Styles.paperContainer.className.value,
              Grid(container = true, spacing = 0)(
                Grid(item = true, xs = 12, sm = 10, md = 8)(
                  TextField(
                    value = s.request.itunesUrl,
                    label = "iTunes Podcast URL",
                    placeholder = "https://itunes.apple.com/us/podcast/foocast/id123456789",
                    helperText = "Enter the iTunes URL for the podcast.",
                    onChange = newValue => {
                      bs.setState(
                        s.copy(request = s.request.copy(itunesUrl = newValue.target.value)))
                    },
                    autoFocus = true,
                    fullWidth = true
                  )()
                ),
                Grid(item = true, xs = 12)(
                  Grid(container = true, spacing = 0, justify = Grid.Justify.FlexEnd)(
                    Grid(item = true)(
                      Button(
                        raised = true,
                        onClick = Callback {
                          bs.setState(s.copy(requestInFlight = true)).runNow
                          DoteProtoServer.addPodcast(s.request) onComplete {
                            case Success(apiResponse) =>
                              bs.setState(s.copy(response = apiResponse, requestInFlight = false))
                                .runNow()
                            case Failure(t) => throw t
                          }
                        }
                      )("Submit")
                    )
                  )
                )
              )
            ),
            if (s.requestInFlight) {
              LinearProgress(mode = LinearProgress.Mode.Indeterminate,
                             className = Styles.linearProgress)()
            } else {
              <.div()
            }
          )
        ),
        Grid(item = true, xs = 12)(
          // TODO handle more than one response
          s.response.podcasts.headOption match {
            case Some(dotable) => {
              Fade(in = true, transitionDurationMs = 1000)(
                Grid(container = true, spacing = 0)(
                  Grid(item = true, xs = 12)(
                    Typography(typographyType = Typography.Type.SubHeading)(
                      s"Added ${dotable.getCommon.title}"
                    )
                  ),
                  Grid(item = true, xs = 12)(
                    <.div(^.className := Styles.tileContainer.className.value,
                          EntityTile.component(EntityTile.Props(routerCtl, dotable = dotable)))
                  ),
                  Grid(item = true, xs = 12)(
                    EntityDetails.component(EntityDetails.Props(routerCtl, dotable))
                  )
                ))
            }
            case None => ""
          }
        )
      )
  }

  val component = ScalaComponent
    .builder[DoteRouterCtl]("AddPodcastView")
    .initialState(State(AddPodcastRequest(), AddPodcastResponse()))
    .backend(new Backend(_))
    .render(x => x.backend.render(x.props, x.state))
    .build

  def apply(routerCtl: DoteRouterCtl) = component(routerCtl)
}
