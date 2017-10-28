package kurtome.dote.web.components.widgets

import dote.proto.api.dotable.Dotable
import japgolly.scalajs.react._
import japgolly.scalajs.react.vdom.html_<^._
import kurtome.dote.web.Styles
import kurtome.dote.web.components.materialui._
import kurtome.dote.web.components.ComponentHelpers._

import scala.scalajs._

object EntityDetails {

  case class State(tabIndex: Int)

  private case class DetailField(label: String, value: String)

  private case class ExtractedFields(title: String = "",
                                     subtitle: String = "",
                                     summary: String = "",
                                     details: Seq[DetailField])

  private def extractFields(dotable: Dotable): ExtractedFields = {
    val common = dotable.getCommon
    dotable.kind match {
      case Dotable.Kind.PODCAST =>
        val latestEpisode = dotable.getRelatives.children.head
        val podcastDetails = dotable.getDetails.getPodcast
        ExtractedFields(
          title = common.title,
          subtitle = "",
          summary = common.description,
          details = Seq(
            DetailField("Latest Episode", latestEpisode.getCommon.title),
            DetailField("Creator", podcastDetails.author),
            DetailField("Website", podcastDetails.websiteUrl),
            DetailField("Years",
                        epochSecRangeToYearRange(common.publishedEpochSec, common.updatedEpochSec),
            ),
            DetailField("Language", podcastDetails.languageDisplay)
          )
        )
      case Dotable.Kind.PODCAST_EPISODE =>
        val episodeDetails = dotable.getDetails.getPodcastEpisode
        ExtractedFields(
          title = common.title,
          subtitle = epochSecToDate(common.publishedEpochSec),
          summary = common.description,
          details = Seq(DetailField("Duration", durationSecToMin(episodeDetails.durationSec)))
        )
    }
  }

  private def episodesByRecency(dotable: Dotable) = {
    dotable.kind match {
      case Dotable.Kind.PODCAST => dotable.getRelatives.children.reverse
      case _ => Nil
    }
  }

  val func: js.Function2[Int, Boolean, Unit] = (i: Int, foo: Boolean) => {}

  class Backend(bs: BackendScope[Dotable, State]) {
    def handleTabIndexChanged(e: js.Dynamic, newValue: Int) = {
      bs.modState(_.copy(tabIndex = newValue))
    }

    def render(dotable: Dotable, s: State): VdomElement = {
      val fields = extractFields(dotable)

      Paper(className = Styles.detailsRoot)(
        Grid(container = true, spacing = 24, align = Grid.Align.Center)(
          Grid(item = true, xs = 12, lg = 4, className = Styles.titleFieldContainer)(
            Typography(typographyType = Typography.Type.Headline)(fields.title),
            Typography(typographyType = Typography.Type.SubHeading)(fields.subtitle),
          ),
          Grid(item = true, xs = 12, lg = 4)(
            <.div(^.className := Styles.centerContainer.className.value,
                  EntityTile.component(EntityTile.Props(dotable, size = "250px")))
          ),
          Grid(item = true, xs = 12, lg = 4)(
            Typography(typographyType = Typography.Type.Body1)(fields.summary)
          ),
          Grid(item = true, xs = 12)(
            Tabs(value = s.tabIndex, onChange = handleTabIndexChanged)(
              Tab(label = "Info")(),
              Tab(label = "Episodes")()
            )),
          Grid(item = true, xs = 12, className = Styles.podcastDetailsTabContentsContainer)(
            if (s.tabIndex == 0) {
              Grid(container = true,
                   spacing = 24,
                   align = Grid.Align.FlexStart,
                   className = Styles.detailsFieldContainer)(
                fields.details flatMap { detailField =>
                  Seq(
                    Grid(item = true, xs = 4)(
                      Typography(typographyType = Typography.Type.SubHeading)(detailField.label)
                    ),
                    Grid(item = true, xs = 8)(
                      Typography(typographyType = Typography.Type.Body1)(detailField.value)
                    )
                  )
                } toVdomArray
              )
            } else {
              List(dense = true, className = Styles.episodeList).withKey("list")(
                (episodesByRecency(dotable) map { episode =>
                  ListItem(dense = true)(ListItemText(
                    primary = episode.getCommon.title,
                    secondary =
                      s"${durationSecToMin(episode.getDetails.getPodcastEpisode.durationSec)}, ${epochSecToDate(
                        episode.getCommon.publishedEpochSec)}"
                  )())
                }).toVdomArray
              )
            }
          )
        )
      )
    }
  }

  val component = ScalaComponent
    .builder[Dotable](this.getClass.getSimpleName)
    .initialState(State(0))
    .backend(new Backend(_))
    .renderPS((builder, props, state) => builder.backend.render(props, state))
    .build
}
