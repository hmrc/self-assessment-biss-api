/*
 * Copyright 2021 HM Revenue & Customs
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

package v1.controllers

import cats.data.EitherT
import cats.implicits._
import javax.inject.{Inject, Singleton}
import play.api.libs.json.Json
import play.api.mvc.{Action, AnyContent, ControllerComponents}
import play.mvc.Http.MimeTypes
import utils.{IdGenerator, Logging}
import v1.controllers.requestParsers.RetrieveForeignPropertyBISSRequestDataParser
import v1.models.errors._
import v1.models.requestData.RetrieveForeignPropertyBISSRawData
import v1.services.{EnrolmentsAuthService, ForeignPropertyBISSService, MtdIdLookupService}

import scala.concurrent.{ExecutionContext, Future}

@Singleton
class RetrieveForeignPropertyBISSController @Inject()(val authService: EnrolmentsAuthService,
                                                      val lookupService: MtdIdLookupService,
                                                      requestParser: RetrieveForeignPropertyBISSRequestDataParser,
                                                      foreignPropertyBISSService: ForeignPropertyBISSService,
                                                      cc: ControllerComponents,
                                                      val idGenerator: IdGenerator)(implicit ec: ExecutionContext)
  extends AuthorisedController(cc) with BaseController with Logging {

  implicit val endpointLogContext: EndpointLogContext =
    EndpointLogContext(
      controllerName = "RetrieveForeignPropertyBISSController",
      endpointName = "retrieveForeignPropertyBiss"
    )

  def retrieveBiss(nino: String, businessId: Option[String], taxYear: Option[String], typeOfBusiness: Option[String]): Action[AnyContent] =
    authorisedAction(nino).async { implicit request =>

      implicit val correlationId: String = idGenerator.generateCorrelationId
      logger.info(
        s"[${endpointLogContext.controllerName}][${endpointLogContext.endpointName}] " +
          s"with CorrelationId: $correlationId")

      val rawData = RetrieveForeignPropertyBISSRawData(nino, businessId,typeOfBusiness,taxYear)
      val result =
        for {
          parsedRequest <- EitherT.fromEither[Future](requestParser.parseRequest(rawData))
          response <- EitherT(foreignPropertyBISSService.retrieveBiss(parsedRequest))
        } yield {
          logger.info(
            s"[${endpointLogContext.controllerName}][${endpointLogContext.endpointName}] - " +
              s"Success response received with correlationId: ${response.correlationId}"
          )
          Ok(Json.toJson(response.responseData))
            .withApiHeaders(response.correlationId)
            .as(MimeTypes.JSON)
        }
      result.leftMap { errorWrapper =>
        val resCorrelationId = errorWrapper.correlationId
        val result = errorResult(errorWrapper).withApiHeaders(resCorrelationId)
        logger.info(
          s"[${endpointLogContext.controllerName}][${endpointLogContext.endpointName}] - " +
            s"Error response received with CorrelationId: $resCorrelationId")

        result
      }.merge
    }

  private def errorResult(errorWrapper: ErrorWrapper) = {
    (errorWrapper.error: @unchecked) match {
      case BadRequestError | NinoFormatError | BusinessIdFormatError | TaxYearFormatError | TypeOfBusinessFormatError | RuleTaxYearNotSupportedError
           | RuleTaxYearRangeInvalidError | RuleForeignBusinessIdError | RuleTypeOfBusinessError =>
        BadRequest(Json.toJson(errorWrapper))
      case NotFoundError => NotFound(Json.toJson(errorWrapper))
      case DownstreamError => InternalServerError(Json.toJson(errorWrapper))
    }
  }
}