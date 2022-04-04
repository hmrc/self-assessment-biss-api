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

package v1.services

import javax.inject.{Inject, Singleton}
import cats.implicits._
import cats.data.EitherT
import uk.gov.hmrc.http.HeaderCarrier
import utils.Logging
import v1.connectors.ForeignPropertyBISSConnector
import v1.controllers.EndpointLogContext
import v1.models.errors._
import v1.models.outcomes.ResponseWrapper
import v1.models.requestData.RetrieveForeignPropertyBISSRequest
import v1.models.response.RetrieveForeignPropertyBISSResponse
import v1.support.DesResponseMappingSupport

import scala.concurrent.{ExecutionContext, Future}

@Singleton
class ForeignPropertyBISSService @Inject() (connector: ForeignPropertyBISSConnector) extends DesResponseMappingSupport with Logging {

  def retrieveBiss(request: RetrieveForeignPropertyBISSRequest)(implicit
      hc: HeaderCarrier,
      ec: ExecutionContext,
      logContext: EndpointLogContext,
      correlationId: String): Future[Either[ErrorWrapper, ResponseWrapper[RetrieveForeignPropertyBISSResponse]]] = {

    val result = for {
      desResponseWrapper <- EitherT(connector.retrieveBiss(request)).leftMap(mapDesErrors(mappingDesToMtdError))
    } yield desResponseWrapper.map(des => des)

    result.value
  }

  private def mappingDesToMtdError: Map[String, MtdError] =
    Map(
      "INVALID_IDVALUE"          -> NinoFormatError,
      "INVALID_TAXYEAR"          -> TaxYearFormatError,
      "INVALID_INCOMESOURCEID"   -> BusinessIdFormatError,
      "NOT_FOUND"                -> NotFoundError,
      "INVALID_IDTYPE"           -> DownstreamError,
      "INVALID_INCOMESOURCETYPE" -> TypeOfBusinessFormatError,
      "SERVER_ERROR"             -> DownstreamError,
      "SERVICE_UNAVAILABLE"      -> DownstreamError
    )

}
