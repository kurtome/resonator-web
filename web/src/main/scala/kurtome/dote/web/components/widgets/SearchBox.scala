package kurtome.dote.web.components.widgets

import dote.proto.api.action.search.SearchRequest

import scalacss.internal.mutable.StyleSheet
import scala.scalajs.js
import scala.scalajs.js._
import scala.scalajs.js.JSConverters._
import dote.proto.api.dotable.Dotable
import japgolly.scalajs.react._
import japgolly.scalajs.react.raw.SyntheticEvent
import japgolly.scalajs.react.vdom.html_<^._
import kurtome.dote.web.SharedStyles
import kurtome.dote.web.DoteRoutes.{DoteRouterCtl, PodcastRoute}
import kurtome.dote.web.components.materialui._
import kurtome.dote.web.components.ComponentHelpers._
import kurtome.dote.web.CssSettings._
import kurtome.dote.web.components.lib.AutoSuggest
import kurtome.dote.web.components.lib.AutoSuggest._
import kurtome.dote.web.constants.MuiTheme
import kurtome.dote.web.rpc.DoteProtoServer
import kurtome.dote.web.utils.{Debounce, MuiInlineStyleSheet}

import scala.scalajs.js
import scala.concurrent.ExecutionContext.Implicits.global

object SearchBox {

  private object Styles extends StyleSheet.Inline {
    import dsl._

    val contextWrapper = style(
      position.relative,
      marginBottom(SharedStyles.spacingUnit * 2)
    )

    val suggestDropdownSheet = style(
      zIndex := "1",
      position.absolute,
      marginBottom(SharedStyles.spacingUnit),
      left(0 px),
      right(0 px)
    )

    val suggestTitleText = styleF.bool(
      isHighlighted =>
        styleS(
          marginLeft(SharedStyles.spacingUnit * 2),
          whiteSpace.nowrap,
          textOverflow := "ellipsis",
          overflow.hidden,
          maxWidth(500 px),
          color :=! (if (isHighlighted) {
                       MuiTheme.theme.palette.text.primary.asInstanceOf[String]
                     } else {
                       MuiTheme.theme.palette.text.secondary.asInstanceOf[String]

                     }),
          fontSize(if (isHighlighted) 1.1 rem else 1 rem),
          textDecoration := "none"
      ))
  }
  Styles.addToDocument()
  val muiStyles = new MuiInlineStyleSheet(Styles)
  import muiStyles._

  case class Props(routerCtl: DoteRouterCtl)
  case class State(query: String = "", results: Seq[Dotable] = Nil)

  private val l$ = js.Dynamic.literal
  private val autoSuggestTheme: js.Dynamic = l$(
    "container" -> l$(
      "flexGrow" -> 1,
      "position" -> "absolute",
      "width" -> "100%"
    ),
    "suggestion" -> l$(
      "display" -> "block",
      "marginTop" -> "16px",
      "marginBottom" -> "8px"
    ),
    "suggestionsList" -> l$(
      "margin" -> 0,
      "paddingLeft" -> "24px",
      "listStyleType" -> "none"
    ),
    "textField" -> l$(
      "width" -> "100%"
    )
  )

  class Backend(bs: BackendScope[Props, State]) {

    val runSearch: (String) => Unit = Debounce.debounce1(waitMs = 300) { query =>
      DoteProtoServer.search(SearchRequest(query = query, maxResults = 5)) map { response =>
        bs.modState(_.copy(results = response.dotables)).runNow()
      }
    }

    def suggestionsFetchRequested(suggestionFetchRequest: SuggestionFetchRequest): Callback =
      Callback {
        val query = suggestionFetchRequest.value
        val reason = suggestionFetchRequest.reason
        reason match {
          case "input-changed" => {
            runSearch(query)
          }
          case _ => Unit
        }
      }

    def getSuggestionValue(suggestion: Dotable): String = {
      suggestion.getCommon.title
    }

    def renderSuggestion(suggestion: Dotable, params: SuggestionRenderParams): raw.ReactElement = {
      val routerCtl = bs.props.runNow().routerCtl
      val route = PodcastRoute(suggestion.id, suggestion.slug)

      val style = Styles.suggestTitleText(params.isHighlighted)

      Grid(container = true,
           spacing = 0,
           justify = Grid.Justify.FlexStart,
           alignItems = Grid.AlignItems.Center)(
        Grid(item = true)(
          <.span(EntityTile(routerCtl, suggestion, elevation = 0, width = "70px")())),
        Grid(item = true)(
          Typography(style = style.inline, typographyType = Typography.Type.SubHeading)(
            suggestion.getCommon.title))
      ).rawElement
    }

    val renderSuggestionsContainer: Function1[SuggestionContainerRenderParams, raw.ReactElement] =
      (params) => {
        val style = Styles.suggestDropdownSheet.inline
        if (params.children.isDefined) {
          Paper(baseProps = params.containerProps, style = style)(VdomNode(params.children.get)).raw
        } else {
          Paper(baseProps = params.containerProps, style = style)().raw
        }
      }

    val onSuggestionSelected
      : Function2[ReactEventFromInput, SuggestionSelectedParams[Dotable], Unit] =
      (e, params) => {
        val routerCtl = bs.props.runNow().routerCtl
        val route = PodcastRoute(params.suggestion.id, params.suggestion.slug)
        routerCtl.set(route).runNow()
      }

    val suggestionClearRequested: Callback = Callback {}

    def handleChange(e: ReactEventFromInput): Callback = {
      val target = e.target.asInstanceOf[js.Dynamic]
      if (js.isUndefined(target.value)) {
        Callback.empty
      } else {
        val query = e.target.value
        bs.modState(_.copy(query = query))
      }
    }

    val renderInput: js.Function1[InputProps, raw.ReactElement] = (inputProps) => {
      TextField(
        value = inputProps.value,
        placeholder = inputProps.placeholder,
        inputType = inputProps.inputType,
        fullWidth = true,
        onChange = (e) => Callback(inputProps.onChange(e)),
        inputRef = inputProps.ref
      )().raw
    }

    def render(p: Props, s: State): VdomElement = {
      val suggestions: js.Array[Dotable] = s.results.toJSArray
      <.div(
        ^.className := Styles.contextWrapper,
        AutoSuggest[Dotable](
          suggestions = suggestions,
          onSuggestionsFetchRequested = suggestionsFetchRequested,
          onSuggestionsClearRequested = suggestionClearRequested,
          onSuggestionSelected = onSuggestionSelected,
          getSuggestionValue = getSuggestionValue,
          renderSuggestion = renderSuggestion,
          inputProps = InputProps(value = s.query,
                                  inputType = "search",
                                  onChange = handleChange,
                                  placeholder = "Search by podcast title."),
          theme = autoSuggestTheme,
          renderInputComponent = renderInput,
          renderSuggestionsContainer = renderSuggestionsContainer,
          alwaysRenderSuggestions = true,
          highlightFirstSuggestion = true
        )()
      )
    }
  }

  val component = ScalaComponent
    .builder[Props](this.getClass.getSimpleName)
    .initialState(State())
    .backend(new Backend(_))
    .renderPS((builder, p, s) => builder.backend.render(p, s))
    .build

  def apply(routerCtl: DoteRouterCtl) =
    component.withProps(Props(routerCtl))
}
