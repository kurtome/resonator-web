package kurtome.dote.web.components.widgets

import japgolly.scalajs.react._
import japgolly.scalajs.react.component.builder.Lifecycle
import japgolly.scalajs.react.vdom.html_<^._
import kurtome.dote.web.constants.MuiTheme
import org.scalajs.dom

import scala.scalajs.js
import scala.scalajs.js.annotation.JSImport

object AudioWave {

  @js.native
  trait SiriWaveOptions extends js.Object {
    var container: dom.Element = js.native
    var style: js.UndefOr[String] = js.native
    var speed: js.UndefOr[Float] = js.native
    var amplitude: js.UndefOr[Float] = js.native
    var frequency: js.UndefOr[Float] = js.native
    var cover: js.UndefOr[Boolean] = js.native
    var color: js.UndefOr[String] = js.native
    var speedInterpolationSpeed: js.UndefOr[Float] = js.native
    var amplitudeInterpolationSpeed: js.UndefOr[Float] = js.native
  }

  /**
    * https://github.com/caffeinalab/siriwavejs
    */
  @JSImport("siriwavejs/SiriWave", JSImport.Default)
  @js.native
  class SiriWave(options: SiriWaveOptions) extends js.Object {
    def start(): Unit = js.native
    def stop(): Unit = js.native
    def setAmplitude(amplitude: Float): Unit = js.native
    def setSpeed(speed: Float): Unit = js.native
  }

  private def createWave(
      container: dom.Element,
      style: js.UndefOr[String] = js.undefined,
      speed: js.UndefOr[Float] = js.undefined,
      amplitude: js.UndefOr[Float] = js.undefined,
      frequency: js.UndefOr[Float] = js.undefined,
      cover: js.UndefOr[Boolean] = js.undefined,
      color: js.UndefOr[String] = js.undefined,
      speedInterpolationSpeed: js.UndefOr[Float] = js.undefined,
      amplitudeInterpolationSpeed: js.UndefOr[Float] = js.undefined): SiriWave = {
    val options = new js.Object().asInstanceOf[SiriWaveOptions]
    options.container = container
    options.style = style
    options.speed = speed
    options.amplitude = amplitude
    options.frequency = frequency
    options.cover = cover
    options.color = color
    options.speedInterpolationSpeed = speedInterpolationSpeed
    options.amplitudeInterpolationSpeed = amplitudeInterpolationSpeed
    new SiriWave(options)
  }

  case class Props(width: String, height: String, started: Boolean)

  class Backend(bs: BackendScope[Props, Unit]) {

    var wave: SiriWave = null

    val onMount = Callback {
      wave = createWave(container = bs.getDOMNode.runNow(),
                        cover = true,
                        color = "#999999",
                        speed = 0.05f,
                        amplitude = amplitude(bs.props.runNow()))
      wave.start()
    }

    def amplitude(p: Props): Float = {
      if (p.started) 0.25f else 0f
    }

    def render(p: Props): VdomElement = {
      if (wave != null) {
        wave.setAmplitude(amplitude(p))
      }
      <.div(^.width := p.width, ^.height := p.height, ^.display := "relative")
    }
  }

  val component = ScalaComponent
    .builder[Props](this.getClass.getSimpleName)
    .backend(new Backend(_))
    .renderP((builder, p) => builder.backend.render(p))
    .componentDidMount(x => x.backend.onMount)
    .build

  def apply(width: String = "100%", height: String = "100%", started: Boolean = true) =
    component(Props(width, height, started))
}
