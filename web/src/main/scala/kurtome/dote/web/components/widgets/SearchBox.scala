package kurtome.dote.web.components.widgets

import kurtome.dote.proto.api.action.search.SearchRequest

import scalacss.internal.mutable.StyleSheet
import scala.scalajs.js._
import scala.scalajs.js.JSConverters._
import kurtome.dote.proto.api.dotable.Dotable
import japgolly.scalajs.react._
import japgolly.scalajs.react.vdom.html_<^._
import kurtome.dote.web.SharedStyles
import kurtome.dote.web.DoteRoutes._
import kurtome.dote.web.components.materialui._
import kurtome.dote.web.components.ComponentHelpers._
import kurtome.dote.web.CssSettings._
import kurtome.dote.web.components.lib.AutoSuggest
import kurtome.dote.web.components.lib.AutoSuggest._
import kurtome.dote.web.constants.MuiTheme
import kurtome.dote.web.rpc.DoteProtoServer
import kurtome.dote.web.utils._
import wvlet.log.LogSupport

import scala.scalajs.js
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

object SearchBox {

  object Styles extends StyleSheet.Inline {
    import dsl._

    val contextWrapper = style(
      position.relative
    )

    val suggestDropdownSheet = style(
      zIndex := "1",
      position.absolute,
      marginBottom(SharedStyles.spacingUnit),
      left(0 px),
      right(0 px)
    )

    val suggestItemRoot = styleF.bool(
      isXs =>
        styleS(
          position.relative,
          height(if (isXs) 50 px else 70 px)
      )
    )

    val suggestItemContainer = style(
      height(100 %%),
      position.absolute
    )

    val suggestTitleContainer = style(
      width(75 %%)
    )

    val suggestTitleText = styleF.bool(
      isHighlighted =>
        styleS(
          marginLeft(SharedStyles.spacingUnit * 2),
          whiteSpace.nowrap,
          textOverflow := "ellipsis",
          verticalAlign.middle,
          overflow.hidden,
          width(unset),
          color :=! (if (isHighlighted) {
                       MuiTheme.theme.palette.text.primary
                     } else {
                       MuiTheme.theme.palette.text.secondary
                     }),
          fontSize(if (isHighlighted) 1.2 rem else 1 rem),
          textDecoration := "none"
      ))

    val searchIcon = style(
      float.right
    )

    val textFieldWrapper = style(
      overflow.auto
    )
  }

  case class Props(onResultsUpdated: (String, Seq[Dotable]) => Callback)
  case class State(query: String = "", results: Seq[Dotable] = Nil, inFlight: Seq[Future[_]] = Nil) {
    def isLoading = inFlight.nonEmpty
  }

  private val l$ = js.Dynamic.literal
  private val autoSuggestTheme: js.Dynamic = l$(
    "container" -> l$(
      "flexGrow" -> 1,
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

  class Backend(bs: BackendScope[Props, State]) extends BaseBackend(Styles) {

    val runSearch: (String) => Unit = Debounce.debounce1(waitMs = 300) { query =>
      val f = DoteProtoServer.search(SearchRequest(query = query, maxResults = 5)) map {
        response =>
          // only use the results if the query hasn't changed
          if (bs.state.runNow().query == query) {
            bs.modState(s => s.copy(results = response.dotables)).runNow()
            // invoke the results callback
            bs.props.runNow().onResultsUpdated(query, response.dotables).runNow()
          }
      }

      // Add future to those in flight
      bs.modState(s => s.copy(inFlight = s.inFlight :+ f)).runNow()

      // Regardless of the result, remove the future from those in flight
      f andThen {
        case _ =>
          bs.modState(s => s.copy(inFlight = s.inFlight.filter(_ != f))).runNow()
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
      val query = params.query

      val route = DetailsRoute(suggestion.id, suggestion.slug)

      val title = suggestion.getCommon.title

      var textRemaining: String = title
      var textNodes: VdomArray = VdomArray()
      while (textRemaining.nonEmpty && query.nonEmpty) {
        val i = textRemaining.toLowerCase.indexOf(query.toLowerCase)
        if (i < 0) {
          if (textRemaining.nonEmpty) {
            textNodes += <.i(^.key := "search-prefix-text-remaining", textRemaining)
          }
          textRemaining = ""
        } else {
          if (i > 0) {
            textNodes += <.i(^.key := "search-text-matching", textRemaining.substring(0, i))
          }
          textNodes += <.span(^.key := "search-postfix-text-remaining",
                              textRemaining.substring(i, i + query.length))
          textRemaining = textRemaining.substring(i + query.length)
        }
      }

      val breakpoint = currentBreakpointString

      val tileWidthPx: Int = breakpoint match {
        case "xs" => 50
        case _ => 70
      }

      <.div(
        ^.className := Styles.suggestItemRoot(breakpoint == "xs"),
        Grid(container = true,
             style = Styles.suggestItemContainer,
             spacing = 0,
             justify = Grid.Justify.FlexStart,
             alignItems = Grid.AlignItems.Center)(
          Grid(item = true)(<.span(EntityImage(suggestion, width = asPxStr(tileWidthPx))())),
          Grid(item = true, style = Styles.suggestTitleContainer)(
            Typography(style = Styles.suggestTitleText(params.isHighlighted),
                       variant = Typography.Variants.SubHeading)(
              textNodes
            )
          )
        )
      ).rawElement
    }

    val renderSuggestionsContainer: Function1[SuggestionContainerRenderParams, raw.ReactElement] =
      (params) => {
        val style = Styles.suggestDropdownSheet
        if (params.children.isDefined) {
          Paper(baseProps = params.containerProps, style = style)(VdomNode(params.children.get)).raw
        } else {
          Paper(baseProps = params.containerProps, style = style)().raw
        }
      }

    val onSuggestionSelected
      : Function2[ReactEventFromInput, SuggestionSelectedParams[Dotable], Unit] =
      (e, params) => {
        val route = DetailsRoute(params.suggestion.id, params.suggestion.slug)
        doteRouterCtl.set(route).runNow()
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

    def renderInput(s: State): js.Function1[InputProps, raw.ReactElement] = (inputProps) => {
      <.div(
        IconButton(style = Styles.searchIcon)(Icons.Search()),
        <.div(
          ^.className := Styles.textFieldWrapper,
          TextField(
            value = inputProps.value,
            placeholder = inputProps.placeholder,
            inputType = inputProps.inputType,
            fullWidth = true,
            autoFocus = true,
            onChange = (e) => Callback(inputProps.onChange(e)),
            inputRef = inputProps.ref
          )()
        ),
        <.div(
          ^.visibility := (if (s.isLoading) "visible" else "hidden"),
          Fade(in = s.isLoading, timeoutMs = 1000)(LinearProgress()())
        )
      ).rawElement
    }

    def render(p: Props, s: State): VdomElement = {
      val suggestions: js.Array[Dotable] = s.results.toJSArray
      <.div( // wrap the absolutely positioned element, so that this component is still in the flow
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
            renderInputComponent = renderInput(s),
            renderSuggestionsContainer = renderSuggestionsContainer,
            alwaysRenderSuggestions = true,
            highlightFirstSuggestion = true
          )()
        ))
    }
  }

  val component = ScalaComponent
    .builder[Props](this.getClass.getSimpleName)
    .initialState(State())
    .backend(new Backend(_))
    .renderPS((builder, p, s) => builder.backend.render(p, s))
    .build

  def apply(onResultsUpdated: (String, Seq[Dotable]) => Callback) =
    component.withProps(Props(onResultsUpdated))
}
