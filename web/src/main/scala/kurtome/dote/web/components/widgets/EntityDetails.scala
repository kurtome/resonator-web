package kurtome.dote.web.components.widgets

import dote.proto.model.dote_entity.DoteEntity
import japgolly.scalajs.react._
import japgolly.scalajs.react.component.Generic.{Mounted, Unmounted}
import japgolly.scalajs.react.component.Js.Component
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

  private def extractFields(entity: DoteEntity): ExtractedFields = {
    val common = entity.common.get
    entity.details match {
      case podcast: DoteEntity.Details.Podcast =>
        val latestEpisode = podcast.value.episodes.last
        ExtractedFields(
          title = common.title,
          subtitle = "",
          summary = common.descriptionHtml,
          details = Seq(
            DetailField("Latest Episode", latestEpisode.common.get.title),
            DetailField("Creator", common.createdBy),
            DetailField("Website", podcast.value.websiteUrl),
            DetailField("Years",
                        epochSecRangeToYearRange(common.publishedEpochSec, common.updatedEpochSec),
            ),
            DetailField("Language", podcast.value.languageDisplay)
          )
        )
      case episode: DoteEntity.Details.PodcastEpisode =>
        ExtractedFields(
          title = common.title,
          subtitle = epochSecToDate(common.publishedEpochSec),
          summary = common.descriptionHtml,
          details = Seq(DetailField("Duration", durationSecToMin(episode.value.durationSec)))
        )
    }
  }

  private def episodesByRecency(entity: DoteEntity) = {
    entity.details.podcast.get.episodes.reverse
  }

  val func: js.Function2[Int, Boolean, Unit] = (i: Int, foo: Boolean) => {}

  class Backend(bs: BackendScope[DoteEntity, State]) {
    def handleTabIndexChanged(e: js.Dynamic, newValue: Int) = {
      bs.modState(_.copy(tabIndex = newValue))
    }

    def render(entity: DoteEntity, s: State): VdomElement = {
      val fields = extractFields(entity)

      Paper(className = Styles.detailsRoot)(
        Grid(container = true, spacing = 24, align = Grid.Align.Center)(
          Grid(item = true, xs = 12, lg = 4, className = Styles.titleFieldContainer)(
            Typography(typographyType = Typography.Type.Headline)(fields.title),
            Typography(typographyType = Typography.Type.SubHeading)(fields.subtitle),
          ),
          Grid(item = true, xs = 12, lg = 4)(
            <.div(^.className := Styles.centerContainer.className.value,
                  EntityTile.component(EntityTile.Props(entity, size = "250px")))
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
                (episodesByRecency(entity) map { episode =>
                  ListItem(dense = true)(ListItemText(
                    primary = episode.common.get.title,
                    secondary =
                      s"${durationSecToMin(episode.details.podcastEpisode.get.durationSec)}, ${epochSecToDate(
                        episode.common.get.publishedEpochSec)}"
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
    .builder[DoteEntity](this.getClass.getSimpleName)
    .initialState(State(0))
    .backend(new Backend(_))
    .renderPS((builder, props, state) => builder.backend.render(props, state))
    .build
}
