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

package v2.retrieveBiss

import api.controllers._
import api.services.{EnrolmentsAuthService, MtdIdLookupService}
import config.AppConfig
import play.api.mvc.{Action, AnyContent, ControllerComponents}
import utils.IdGenerator

import javax.inject.Inject
import scala.concurrent.ExecutionContext

class RetrieveBISSController @Inject() (val authService: EnrolmentsAuthService,
                                        val lookupService: MtdIdLookupService,
                                        validatorFactory: RetrieveBISSValidatorFactory,
                                        service: RetrieveBISSService,
                                        cc: ControllerComponents,
                                        val idGenerator: IdGenerator)(implicit appConfig: AppConfig, ec: ExecutionContext)
    extends AuthorisedController(cc) {

  override val endpointName: String = "retrieve-biss"

  implicit val endpointLogContext: EndpointLogContext =
    EndpointLogContext(
      controllerName = "RetrieveBISSController",
      endpointName = "retrieveBiss"
    )

  def retrieveBiss(nino: String, typeOfBusiness: String, taxYear: String, businessId: String): Action[AnyContent] =
    authorisedAction(nino).async { implicit request =>
      implicit val ctx: RequestContext = RequestContext.from(idGenerator, endpointLogContext)

      val validator = validatorFactory.validator(nino = nino, typeOfBusiness = typeOfBusiness, taxYear = taxYear, businessId = businessId)

      val requestHandler =
        RequestHandler
          .withValidator(validator)
          .withService(service.retrieveBiss)
          .withResultCreator(ResultCreator.plainJson(OK))

      requestHandler.handleRequest()
    }


}
