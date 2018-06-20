/*
 * Copyright (C) 2009-2017 Lightbend Inc. <https://www.lightbend.com>
 */
package resonator.server.filters

import javax.inject.Inject
import javax.inject.Singleton

import akka.http.scaladsl.model.StatusCode
import play.api.Logger
import play.api.http.Status
import play.api.mvc._

@Singleton
class RedirectNakedDomainFilter @Inject()() extends EssentialFilter {

  private val logger = Logger(getClass)

  override def apply(next: EssentialAction): EssentialAction = EssentialAction { req =>
    import play.api.libs.streams.Accumulator
    if (req.domain.startsWith("www.")) {
      Accumulator.done(Results.Redirect(createHttpsRedirectUrl(req), Status.TEMPORARY_REDIRECT))
    } else {
      next(req)
    }
  }

  protected def createHttpsRedirectUrl(req: RequestHeader): String = {
    import req.secure
    import req.path

    val newDomain = req.domain.substring(4)
    if (secure) {
      s"https://$newDomain$path"
    } else {
      s"http://$newDomain$path"
    }
  }
}
