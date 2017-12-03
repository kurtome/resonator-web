package kurtome.dote.web.components.lib

import japgolly.scalajs.react._

import scala.scalajs.js
import scala.scalajs.js.annotation.{JSImport, JSName}

/**
  * Wrapper for https://github.com/moroshko/react-autosuggest
  */
object AutoSuggest {

  @JSImport("react-autosuggest", JSImport.Default)
  @js.native
  object RawComponent extends js.Object

  @js.native
  trait SuggestionFetchRequest extends js.Object {

    /**
      * the current value of the input
      */
    val value: String = js.native

    /**
      * string describing why onSuggestionsFetchRequested was called. The possible values are:
      * 'input-changed' - user typed something
      * 'input-focused' - input was focused
      * 'escape-pressed' - user pressed Escape to clear the input (and suggestions are shown for empty input)
      * 'suggestions-revealed' - user pressed Up or Down to reveal suggestions
      * 'suggestion-selected' - user selected a suggestion when alwaysRenderSuggestions={true}
      */
    val reason: String = js.native
  }

  @js.native
  trait SuggestionRenderParams extends js.Object {

    /**
      * Used to highlight the matching string. As user types in the input, query will be equal to
      * the trimmed value of the input. Then, if user interacts using the Up or Down keys, the input
      * will get the value of the highlighted suggestion, but query will remain to be equal to the
      * trimmed value of the input prior to the Up and Down interactions.
      */
    val query: String = js.native

    /**
      * Whether or not the suggestion is highlighted
      */
    val isHighlighted: Boolean = js.native
  }

  @js.native
  trait SuggestionContainerRenderParams extends js.Object {
    val containerProps: js.Dynamic = js.native
    val children: js.UndefOr[raw.ReactElement] = js.native
    val query: String = js.native
  }

  @js.native
  trait SuggestionSelectedParams[TSuggestion] extends js.Object {

    /**
      * the selected suggestion
      */
    val suggestion: TSuggestion = js.native

    /**
      * the value of the selected suggestion (equivalent to getSuggestionValue(suggestion))
      */
    val suggestionValue: String = js.native

    /**
      * the index of the selected suggestion in the suggestions array
      */
    val suggestionIndex: Int = js.native

    /**
      * when rendering multiple sections, this will be the section index (in suggestions) of the
      * selected suggestion. Otherwise, it will be null.
      */
    val sectionIndex: Int = js.native

    /**
      *  string describing how user selected the suggestion. The possible values are:
      *  'click' - user clicked (or tapped) on the suggestion
      *  'enter' - user selected the suggestion using Enter
      */
    val method: String = js.native
  }

  @js.native
  trait InputProps extends js.Object {
    var value: String = js.native
    @JSName("type")
    var inputType: String = js.native
    var placeholder: js.UndefOr[String] = js.native
    var onChange: js.Function1[ReactEventFromInput, Unit] = js.native
    var onBlur: js.Function1[ReactEvent, Unit] = js.native
    var ref: js.Any = js.native
  }
  object InputProps {
    def apply(value: String,
              inputType: String,
              placeholder: js.UndefOr[String] = js.undefined,
              onChange: (ReactEventFromInput) => Callback = (e) => Callback.empty,
              onBlur: (ReactEvent) => Callback = (e) => Callback.empty): InputProps = {
      val p = (new js.Object).asInstanceOf[InputProps]
      p.value = value
      p.inputType = inputType
      p.placeholder = placeholder
      p.onChange = (e) => onChange(e).runNow()
      p.onBlur = (e) => onBlur(e).runNow()
      p
    }
  }

  // NOTE: not all props exposed
  @js.native
  trait Props[TSuggestion] extends js.Object {
    var suggestions: js.Array[TSuggestion] = js.native
    var onSuggestionsFetchRequested: js.Function1[SuggestionFetchRequest, Unit] = js.native
    var onSuggestionsClearRequested: js.Function0[Unit] = js.native
    var getSuggestionValue: js.Function1[TSuggestion, String] = js.native
    var renderSuggestion: js.Function2[TSuggestion, SuggestionRenderParams, raw.ReactElement] =
      js.native
    var onSuggestionSelected: js.UndefOr[
      js.Function2[ReactEventFromInput, SuggestionSelectedParams[TSuggestion], Unit]] =
      js.native
    var inputProps: InputProps = js.native
    var renderInputComponent: js.UndefOr[js.Function1[InputProps, raw.ReactElement]] = js.native
    var renderSuggestionsContainer
      : js.UndefOr[js.Function1[SuggestionContainerRenderParams, raw.ReactElement]] =
      js.native
    var alwaysRenderSuggestions: js.UndefOr[Boolean] = js.native
    var highlightFirstSuggestion: js.UndefOr[Boolean] = js.native
    var theme: js.UndefOr[js.Dynamic] = js.native
  }

  val component = JsComponent[Props[_], Children.Varargs, Null](RawComponent)

  def apply[TSuggestion](
      suggestions: js.Array[TSuggestion],
      onSuggestionsFetchRequested: (SuggestionFetchRequest) => Callback,
      onSuggestionsClearRequested: Callback,
      getSuggestionValue: (TSuggestion) => String,
      renderSuggestion: (TSuggestion, SuggestionRenderParams) => raw.ReactElement,
      onSuggestionSelected: js.UndefOr[
        js.Function2[ReactEventFromInput, SuggestionSelectedParams[TSuggestion], Unit]] =
        js.undefined,
      inputProps: InputProps,
      theme: js.UndefOr[js.Dynamic],
      alwaysRenderSuggestions: js.UndefOr[Boolean] = js.undefined,
      highlightFirstSuggestion: js.UndefOr[Boolean] = js.undefined,
      renderInputComponent: js.UndefOr[js.Function1[InputProps, raw.ReactElement]] = js.undefined,
      renderSuggestionsContainer: js.UndefOr[
        js.Function1[SuggestionContainerRenderParams, raw.ReactElement]] = js.undefined) = {
    val p = (new js.Object).asInstanceOf[Props[TSuggestion]]
    p.suggestions = suggestions
    p.onSuggestionsFetchRequested = (r) => onSuggestionsFetchRequested(r).runNow()
    p.onSuggestionsClearRequested = onSuggestionsClearRequested.toJsFn
    p.getSuggestionValue = getSuggestionValue
    p.renderSuggestion = renderSuggestion
    p.onSuggestionSelected = onSuggestionSelected
    p.inputProps = inputProps
    p.theme = theme
    p.renderInputComponent = renderInputComponent
    p.renderSuggestionsContainer = renderSuggestionsContainer
    p.alwaysRenderSuggestions = alwaysRenderSuggestions
    p.highlightFirstSuggestion = highlightFirstSuggestion
    component.withProps(p)
  }
}
