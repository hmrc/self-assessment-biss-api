/*
 * Copyright 2022 HM Revenue & Customs
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

import cats.data.EitherT
import play.api.libs.json.Json
import play.api.mvc.{Action, AnyContent, ControllerComponents}
import utils.{IdGenerator, Logging}
import v2.controllers.requestParsers.RetrieveBISSRequestDataParser
import v2.models.errors._
import v2.models.requestData.RetrieveBISSRawData
import v2.services.{EnrolmentsAuthService, MtdIdLookupService, RetrieveBISSService}

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class RetrieveBISSController @Inject()(val authService: EnrolmentsAuthService,
                                       val lookupService: MtdIdLookupService,
                                       requestParser: RetrieveBISSRequestDataParser,
                                       retrieveBISSService: RetrieveBISSService,
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
      implicit val correlationId: String = idGenerator.generateCorrelationId
      logger.info(
        s"[${endpointLogContext.controllerName}][${endpointLogContext.endpointName}] " +
          s"with CorrelationId: $correlationId")

      val rawData = RetrieveBISSRawData(nino = nino, typeOfBusiness = typeOfBusiness, taxYear = taxYear, businessId = businessId)

      val result =
        for {
          parsedRequest <- EitherT.fromEither[Future](requestParser.parseRequest(rawData))
          response      <- EitherT(retrieveBISSService.retrieveBiss(parsedRequest, correlationId))
        } yield {
          logger.info(
            s"[${endpointLogContext.controllerName}][${endpointLogContext.endpointName}] - " +
              s"Success response received with correlationId: ${response.correlationId}"
          )

          Ok(Json.toJson(response.responseData))
            .withApiHeaders(response.correlationId)
        }

      result.leftMap { errorWrapper =>
        logger.warn(
          s"[${endpointLogContext.controllerName}][${endpointLogContext.endpointName}] - " +
            s"Error response received with CorrelationId: ${errorWrapper.correlationId}")

        errorResult(errorWrapper)
          .withApiHeaders(errorWrapper.correlationId)
      }.merge
    }

  private def errorResult(errorWrapper: ErrorWrapper) =
    errorWrapper.error match {
      case BadRequestError | NinoFormatError | BusinessIdFormatError | TaxYearFormatError | TypeOfBusinessFormatError | RuleTaxYearNotSupportedError |
           RuleTaxYearRangeInvalidError | RuleTypeOfBusinessError =>
        BadRequest(Json.toJson(errorWrapper))
      case RuleNoIncomeSubmissionsExist => Forbidden(Json.toJson(errorWrapper))
      case NotFoundError                => NotFound(Json.toJson(errorWrapper))
      case DownstreamError              => InternalServerError(Json.toJson(errorWrapper))
      case _                            => unhandledError(errorWrapper)
    }

}
