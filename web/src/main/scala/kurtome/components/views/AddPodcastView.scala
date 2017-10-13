package kurtome.components.views

import dote.proto.action.addpodcast._
import dote.proto.model.doteentity._
import japgolly.scalajs.react._
import japgolly.scalajs.react.vdom.html_<^._
import kurtome.Styles
import kurtome.api.DoteProtoApi
import kurtome.components.materialui._
import kurtome.components.widgets.EntityTile

import scala.concurrent.ExecutionContext.Implicits.global
import scala.util.{Failure, Success}

object AddPodcastView {

  case class State(request: AddPodcastRequest, response: AddPodcastResponse)

  class Backend(bs: BackendScope[Unit, State]) {
    def render(s: State): VdomElement =
      <.div(
        Grid(container = true, justify = Grid.Justify.Center, spacing = 24)(
          Grid(item = true, xs = 12, sm = 10, md = 8)(
            Grid(container = true, justify = Grid.Justify.Center, spacing = 24)(
              Grid(item = true, xs = 6)(
                Typography(typographyType = Typography.Type.Title)("Add Podcast")
              ),
              Grid(item = true, xs = 12)(
                Paper(className = Styles.paperContainer.className)(
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
                              DoteProtoApi.addPodcast(s.request) onComplete {
                                case Success(apiResponse) => {
                                  println(apiResponse)
                                  bs.setState(s.copy(response = apiResponse)).runNow()
                                }
                                case Failure(t) => t.printStackTrace()
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
                    s.response.doteEntity match {
                      case Some(entity) => {
                        Fade(in = true, transitionDurationMs = 1000)(
                          Grid(item = true, xs = 12)(
                            Typography(typographyType = Typography.Type.SubHeading)(
                              s"Added ${entity.common.get.title}"
                            ),
                            EntityTile.component(entity)
                          )
                        )
                      }
                      case None => ""
                    }
                  )
                )
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
