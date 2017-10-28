package kurtome.dote.web.components.views

import dote.proto.api.action.add_podcast._
import japgolly.scalajs.react._
import japgolly.scalajs.react.vdom.html_<^._
import kurtome.dote.web.Styles
import kurtome.dote.web.api.DoteProtoApi
import kurtome.dote.web.components.materialui._
import kurtome.dote.web.components.widgets.{EntityDetails, EntityTile}
import kurtome.dote.web.components.ComponentHelpers._

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import scala.util.{Failure, Success}

object AddPodcastView {

  case class State(request: AddPodcastRequest,
                   response: AddPodcastResponse,
                   requestInFlight: Boolean = false)

  class Backend(bs: BackendScope[Unit, State]) {
    def render(s: State): VdomElement =
      <.div(
        Grid(container = true, justify = Grid.Justify.Center)(
          Grid(item = true, xs = 12, sm = 10, md = 8)(
            Grid(container = true, justify = Grid.Justify.Center, spacing = 24)(
              Grid(item = true, xs = 12)(
                Typography(className = Styles.titleText,
                           typographyType = Typography.Type.Display1)("Add Podcast")
              ),
              Grid(item = true, xs = 12)(
                Paper()(
                  <.div(
                    ^.id := "test",
                    ^.className := Styles.paperContainer.className.value,
                    Grid(container = true, spacing = 0)(
                      Grid(item = true, xs = 12, sm = 10, md = 8)(
                        TextField(
                          value = s.request.feedUrl,
                          label = "Feed URL",
                          placeholder = "http://feed.podcast.com",
                          helperText = "Enter the URL for the podcast RSS feed.",
                          onChange = newValue => {
                            bs.setState(
                              s.copy(request = s.request.copy(feedUrl = newValue.target.value)))
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
                                DoteProtoApi.addPodcast(s.request) onComplete {
                                  case Success(apiResponse) =>
                                    bs.setState(s.copy(response = apiResponse,
                                                       requestInFlight = false))
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
                s.response.doteEntity match {
                  case Some(dotable) => {
                    Fade(in = true, transitionDurationMs = 1000)(
                      Grid(container = true, spacing = 0)(
                        Grid(item = true, xs = 12)(
                          Typography(typographyType = Typography.Type.SubHeading)(
                            s"Added ${dotable.common.get.title}"
                          )
                        ),
                        Grid(item = true, xs = 12)(
                          <.div(^.className := Styles.tileContainer.className.value,
                                EntityTile.component(EntityTile.Props(dotable = dotable)))
                        ),
                        Grid(item = true, xs = 12)(
                          EntityDetails.component(dotable)
                        )
                      ))
                  }
                  case None => ""
                }
              )
            )
          )
        )
      )
  }

  val component = ScalaComponent
    .builder[Unit]("AddPodcastView")
    .initialState(State(AddPodcastRequest(), AddPodcastResponse()))
    .renderBackend[Backend]
    .build

  def apply() = component()
}
