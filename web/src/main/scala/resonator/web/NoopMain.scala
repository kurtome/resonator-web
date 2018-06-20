package resonator.web

object NoopMain {

  def main(args: Array[String]): Unit = {
    // Does nothing.
    //  - the single page app uses WebMain.main() which is called from main.scala.html
    //  - the web worker uses its own main from worker.scala.js
  }

}
