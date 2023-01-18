/*
 * Copyright 2023 HM Revenue & Customs
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

package v2.controllers

import api.controllers.{AuthorisedController, BaseController, EndpointLogContext, RequestContext, RequestHandler, ResultCreator}
import api.services.{EnrolmentsAuthService, MtdIdLookupService}
import play.api.mvc.{Action, AnyContent, ControllerComponents}
import utils.{IdGenerator, Logging}
import v2.controllers.requestParsers.RetrieveBISSRequestDataParser
import v2.models.requestData.RetrieveBISSRawData
import v2.services.RetrieveBISSService

import javax.inject.Inject
import scala.concurrent.ExecutionContext

class RetrieveBISSController @Inject() (val authService: EnrolmentsAuthService,
                                        val lookupService: MtdIdLookupService,
                                        requestParser: RetrieveBISSRequestDataParser,
                                        service: RetrieveBISSService,
                                        cc: ControllerComponents,
                                        val idGenerator: IdGenerator)(implicit ec: ExecutionContext)
    extends AuthorisedController(cc)
    with BaseController
    with Logging {

  implicit val endpointLogContext: EndpointLogContext =
    EndpointLogContext(
      controllerName = "RetrieveBISSController",
      endpointName = "retrieveBiss"
    )

  def retrieveBiss(nino: String, typeOfBusiness: String, taxYear: String, businessId: String): Action[AnyContent] =
    authorisedAction(nino).async { implicit request =>
      implicit val ctx: RequestContext = RequestContext.from(idGenerator, endpointLogContext)

      val rawData = RetrieveBISSRawData(nino = nino, typeOfBusiness = typeOfBusiness, taxYear = taxYear, businessId = businessId)

      val requestHandler =
        RequestHandler
          .withParser(requestParser)
          .withService(service.retrieveBiss)
          .withResultCreator(ResultCreator.plainJson(OK))

      requestHandler.handleRequest(rawData)
    }

}
