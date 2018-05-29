package kurtome.dote.web.components.widgets.card

import japgolly.scalajs.react.BackendScope
import japgolly.scalajs.react._
import japgolly.scalajs.react.vdom.VdomElement
import japgolly.scalajs.react.vdom.html_<^._
import kurtome.dote.proto.api.activity.Activity
import kurtome.dote.proto.api.dotable.Dotable
import kurtome.dote.proto.api.dote.Dote
import kurtome.dote.proto.api.dote.Dote.EmoteKind
import kurtome.dote.shared.constants.Emojis
import kurtome.dote.web.CssSettings._
import kurtome.dote.web.DoteRoutes.DetailsRoute
import kurtome.dote.web.DoteRoutes.ProfileRoute
import kurtome.dote.web.SharedStyles
import kurtome.dote.web.components.ComponentHelpers._
import kurtome.dote.web.components.materialui._
import kurtome.dote.web.components.widgets._
import kurtome.dote.web.constants.MuiTheme
import kurtome.dote.web.utils.BaseBackend
import scalacss.ScalaCssReact._
import wvlet.log.LogSupport

object ActivityCard extends LogSupport {

  object Styles extends StyleSheet.Inline {
    import dsl._

    val title = style(
      marginBottom(SharedStyles.spacingUnit)
    )

    val paperWrapper = style(
      backgroundColor :=! MuiTheme.theme.palette.extras.cardHeader
    )

    val headerTextWrapper = style(
      width :=! ("calc(100% - 16px)"), // leave space for the padding
      padding(SharedStyles.spacingUnit),
      display.inlineBlock
    )

    val tileContainer = style(
      marginTop(0 px)
    )

    val accountIcon = style(
      display.inlineFlex,
      marginRight(4 px)
    )

    val reviewSnippet = style(
      marginTop(SharedStyles.spacingUnit)
    )

    val reviewSnippetItem = style(
      paddingRight(4 px),
      maxWidth(100 %%)
    )

    val rating = style(
      width.unset,
      float.right
    )

    val username = style(
      display.inline,
      marginLeft(1.9 em), // left space for account icon
      lineHeight(1.75 em)
    )

    val star = style(
      fontSize(16 px)
    )

    val starsWrapper = style(
      marginRight(8 px)
    )

  }
  Styles.addToDocument()

  case class Props(activity: Activity)
  case class State()

  class Backend(bs: BackendScope[Props, State]) extends BaseBackend(Styles) {

    def renderStar(halfStars: Int, starPosition: Int) = {
      if (halfStars < ((starPosition * 2) - 1)) {
        Icons.StarOutline(style = Styles.star)
      } else if (halfStars == ((starPosition * 2) - 1)) {
        Icons.StarHalf(style = Styles.star)
      } else {
        Icons.Star(style = Styles.star)
      }
    }

    def renderStars(dote: Dote): VdomNode = {
      val halfStars = dote.halfStars
      if (halfStars > 0) {
        Typography(component = "span",
                   variant = Typography.Variants.Caption,
                   style = Styles.starsWrapper)(
          renderStar(halfStars, 1),
          renderStar(halfStars, 2),
          renderStar(halfStars, 3),
          renderStar(halfStars, 4),
          renderStar(halfStars, 5)
        )
      } else {
        <.div()
      }
    }

    def render(p: Props, s: State): VdomElement = {
      val activity = p.activity
      val dotable = activity.getDote.getDotable
      val dote = activity.getDote.getDote
      val review = activity.getDote.review
      val reviewSnippetLength = 50
      val reviewBody: String =
        review
          .map(_.getCommon.description)
          .getOrElse("")
          .take(reviewSnippetLength - 3)
          .padTo(reviewSnippetLength, '.')

      ReactFragment(
        <.div(
          ^.className := Styles.headerTextWrapper,
          GridContainer(spacing = 0, justify = Grid.Justify.SpaceBetween)(
            GridItem(zeroMinWidth = true)(
              GridContainer(wrap = Grid.Wrap.NoWrap, alignItems = Grid.AlignItems.Center)(
                GridItem(style = Styles.accountIcon, zeroMinWidth = true)(Icons.AccountCircle()),
                GridItem(zeroMinWidth = true)(Typography(component = "span", noWrap = true)(
                  SiteLink(ProfileRoute(dote.getPerson.username))(dote.getPerson.username)))
              )
            ),
            GridItem(zeroMinWidth = true)(
              GridContainer(spacing = 0,
                            wrap = Grid.Wrap.NoWrap,
                            alignItems = Grid.AlignItems.Center)(
                GridItem(zeroMinWidth = true)(renderStars(dote)),
                GridItem(zeroMinWidth = true)(
                  Typography(component = "span", noWrap = true)(Emojis.pickEmoji(dote.emoteKind)))
              )
            )
          ),
          Hidden(xsUp = review.isEmpty)(
            Typography(style = Styles.reviewSnippet, variant = Typography.Variants.Caption)(
              s"$reviewBody"),
            Typography(variant = Typography.Variants.Caption)(
              SiteLink(DetailsRoute(review.map(_.id).getOrElse(""), "review"))("See Review")
            )
          )
        ),
        if (dotable.kind == Dotable.Kind.PODCAST) {
          PodcastCard(dotable = dotable, color = PodcastCard.Colors.PrimaryAccent)()
        } else if (dotable.kind == Dotable.Kind.PODCAST_EPISODE) {
          EpisodeCard(dotable = dotable, color = EpisodeCard.Colors.PrimaryAccent)()
        } else {
          // Placeholder for correct spacing
          <.div(^.width := "100%")
        }
      )
    }
  }

  val component = ScalaComponent
    .builder[Props](this.getClass.getSimpleName)
    .initialState(State())
    .backend(new Backend(_))
    .renderPS((builder, props, state) => builder.backend.render(props, state))
    .build

  def apply(activity: Activity) = {
    assert(activity.content.isDote)
    component.withProps(Props(activity))
  }

}
