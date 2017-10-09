package kurtome.components.views

import dote.proto.addpodcast._
import japgolly.scalajs.react._
import japgolly.scalajs.react.vdom.html_<^._
import kurtome.api.DoteProtoApi
import kurtome.components.materialui._

import scala.concurrent.ExecutionContext.Implicits.global

object AddPodcastView {

  case class State(request: AddPodcastRequest, response: AddPodcastResponse)

  class Backend(bs: BackendScope[Unit, State]) {
    def render(s: State): VdomElement =
      <.div(
        VdomArray(
          Grid(container = true, justify = Grid.Justify.Center, spacing = 24)(
            Grid(item = true, xs = 12, sm = 10, md = 8)(
              Grid(container = true, justify = Grid.Justify.Center, spacing = 24)(
                Grid(item = true, xs = 12)(
                  Typography(typographyType = Typography.Type.Title)("Add Podcast")
                ),
                Grid(item = true, xs = 12)(
                  Paper()(
                    Grid(container = true)(
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
                        Grid(container = true, justify = Grid.Justify.FlexEnd)(
                          Grid(item = true)(
                            Button(
                              raised = true,
                              onClick = Callback {
                                DoteProtoApi.addPodcast(s.request) map { apiResponse =>
                                  bs.setState(s.copy(response = apiResponse)).runNow()
                                }
                              }
                            )("Submit")
                          )
                        )
                      )
                    )
                  )
                ),
                Grid(item = true, xs = 12)(
                  Grid(container = true, justify = Grid.Justify.FlexStart)(
                    Grid(item = true)(
                      Typography(typographyType = Typography.Type.SubHeading)(
                        s"Added '${s.response.title}'")
                    )
                  )
                )
              )
            )
          )
        ))
  }

  val component = ScalaComponent
    .builder[Unit]("AddPodcastView")
    .initialState(State(AddPodcastRequest(), AddPodcastResponse()))
    .renderBackend[Backend]
    .build

  def apply() = component()
}
