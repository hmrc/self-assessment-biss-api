/*
 * Copyright 2025 HM Revenue & Customs
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package routing

import api.models.errors.{InvalidAcceptHeaderError, UnsupportedVersionError}
import config.MockAppConfig
import org.apache.pekko.actor.ActorSystem
import org.scalatest.Inside
import org.scalatestplus.play.guice.GuiceOneAppPerSuite
import play.api.http.HeaderNames.ACCEPT
import play.api.http.Status.{NOT_ACCEPTABLE, NOT_FOUND}
import play.api.http.{HttpConfiguration, HttpErrorHandler, HttpFilters}
import play.api.mvc._
import play.api.routing.Router
import play.api.test.FakeRequest
import play.api.test.Helpers.{contentAsJson, status}
import support.UnitSpec

class VersionRoutingRequestHandlerSpec extends UnitSpec with Inside with MockAppConfig with GuiceOneAppPerSuite {
  test =>

  implicit private val actorSystem: ActorSystem = ActorSystem("test")
  val actionBuilder: DefaultActionBuilder       = app.injector.instanceOf[DefaultActionBuilder]

  import play.api.mvc.Handler
  import play.api.routing.sird._

  object DefaultHandler extends Handler
  object V1Handler      extends Handler
  object V2Handler      extends Handler
  object V3Handler      extends Handler

  private val defaultRouter = Router.from { case GET(p"") =>
    DefaultHandler
  }

  private val v2Router = Router.from { case GET(p"/v2") =>
    V2Handler
  }

  private val v3Router = Router.from { case GET(p"/v3") =>
    V3Handler
  }

  private val routingMap = new VersionRoutingMap {
    override val defaultRouter: Router     = test.defaultRouter
    override val map: Map[Version, Router] = Map(Version2 -> v2Router, Version3 -> v3Router)
  }

  class Test(implicit acceptHeader: Option[String]) {
    val httpConfiguration: HttpConfiguration = HttpConfiguration("context")
    private val errorHandler                 = mock[HttpErrorHandler]
    private val filters                      = mock[HttpFilters]
    (() => filters.filters).stubs().returns(Seq.empty)

    val requestHandler: VersionRoutingRequestHandler =
      new VersionRoutingRequestHandler(routingMap, errorHandler, httpConfiguration, mockAppConfig, filters, actionBuilder)

    def buildRequest(path: String): RequestHeader =
      acceptHeader
        .foldLeft(FakeRequest("GET", path)) { (req, accept) =>
          req.withHeaders((ACCEPT, accept))
        }

  }

  "Routing requests with no version" should {
    implicit val acceptHeader: None.type = None
    handleWithDefaultRoutes()
  }

  "Routing requests with valid version" should {
    implicit val acceptHeader: Some[String] = Some("application/vnd.hmrc.2.0+json")
    handleWithDefaultRoutes()
  }

  "Routing requests with v2" should {
    implicit val acceptHeader: Some[String] = Some("application/vnd.hmrc.2.0+json")
    handleWithVersionRoutes("/v2", V2Handler, Version2)
  }

  "Routing requests with v3" should {
    implicit val acceptHeader: Some[String] = Some("application/vnd.hmrc.3.0+json")
    handleWithVersionRoutes("/v3", V3Handler, Version3)
  }

  "Routing requests to non default router with no version" should {
    implicit val acceptHeader: None.type = None
    "return 406" in new Test {

      val request: RequestHeader = buildRequest("/v2")
      inside(requestHandler.routeRequest(request)) { case Some(action: EssentialAction) =>
        val result = action.apply(request)

        status(result) shouldBe NOT_ACCEPTABLE
        contentAsJson(result) shouldBe InvalidAcceptHeaderError.asJson
      }
    }
  }

  "Routing requests with unsupported version" should {
    implicit val acceptHeader: Some[String] = Some("application/vnd.hmrc.5.0+json")

    "return 404" in new Test {
      private val request = buildRequest("/v2")

      inside(requestHandler.routeRequest(request)) { case Some(action: EssentialAction) =>
        val result = action.apply(request)

        status(result) shouldBe NOT_FOUND
        contentAsJson(result) shouldBe UnsupportedVersionError.asJson
      }
    }
  }

  "Routing requests for supported version but not enabled" when {
    implicit val acceptHeader: Some[String] = Some("application/vnd.hmrc.4.0+json")

    "the version has a route for the resource" must {
      "return 404 Not Found" in new Test {

        private val request = buildRequest("/v2")
        inside(requestHandler.routeRequest(request)) { case Some(action: EssentialAction) =>
          val result = action.apply(request)

          status(result) shouldBe NOT_FOUND
          contentAsJson(result) shouldBe UnsupportedVersionError.asJson

        }
      }
    }
  }

  private def handleWithDefaultRoutes()(implicit acceptHeader: Option[String]): Unit = {
    "if the request ends with a trailing slash" when {
      "handler found" should {
        "use it" in new Test {
          requestHandler.routeRequest(buildRequest("/")) shouldBe Some(DefaultHandler)
        }
      }

      "handler not found" should {
        "try without the trailing slash" in new Test {
          requestHandler.routeRequest(buildRequest("")) shouldBe Some(DefaultHandler)
        }
      }
    }
  }

  private def handleWithVersionRoutes(path: String, handler: Handler, version: Version)(implicit acceptHeader: Option[String]): Unit = {
    "if the request ends with a trailing slash" when {
      "handler found" should {
        "use it" in new Test {
          MockedAppConfig.endpointsEnabled(version).returns(true).anyNumberOfTimes()

          requestHandler.routeRequest(buildRequest(s"$path/")) shouldBe Some(handler)
        }
      }

      "handler not found" should {
        "try without the trailing slash" in new Test {
          MockedAppConfig.endpointsEnabled(version).returns(true).anyNumberOfTimes()

          requestHandler.routeRequest(buildRequest(s"$path")) shouldBe Some(handler)
        }
      }
    }
  }

}
