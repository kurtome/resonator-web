package kurtome.components.views

import japgolly.scalajs.react._
import japgolly.scalajs.react.vdom.html_<^._
import kurtome.components.materialui._

object AddPodcastView {
  val component = ScalaComponent
    .static("AddPodcast")(
      <.div(VdomArray(
        Grid(container = true, justify = Grid.Justify.Center)(
          Grid(item = true, xs = 6)(
            TextField(
              label = "Feed URL",
              placeholder = "http://feed.podcast.com",
              helperText = "Enter the URL for the podcast RSS feed.",
              onChange = newValue => Callback(println(s"test ${newValue.target.value}")),
              autoFocus = true,
              fullWidth = true
            )(),
          )
        ),
        Grid(container = true, justify = Grid.Justify.Center)(
          Grid(item = true, container = true, xs = 6, justify = Grid.Justify.FlexEnd)(
            Button(
              raised = true
            )("Submit")
          )
        )
      )))

  def apply() = component()
}
