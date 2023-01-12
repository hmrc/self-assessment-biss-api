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

package v2.services

import cats.data.EitherT
import uk.gov.hmrc.http.HeaderCarrier
import utils.Logging
import v2.connectors.RetrieveBISSConnector
import v2.controllers.EndpointLogContext
import v2.models.errors._
import v2.models.requestData.RetrieveBISSRequest
import v2.models.response.RetrieveBISSResponse
import v2.support.ServiceSupport

import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class RetrieveBISSService @Inject() (connector: RetrieveBISSConnector)(implicit ec: ExecutionContext) extends ServiceSupport with Logging {

  def retrieveBiss(request: RetrieveBISSRequest)(implicit
      hc: HeaderCarrier,
      correlationId: String,
      logContext: EndpointLogContext): Future[ServiceOutcome[RetrieveBISSResponse]] = {

    val result = for {
      desResponseWrapper <- EitherT(connector.retrieveBiss(request)).leftMap(mapDownstreamErrors(downStreamErrors))
    } yield desResponseWrapper

    result.value
  }

  private val mappingToMtdError = Map(
    "INVALID_IDVALUE"              -> NinoFormatError,
    "INVALID_TAXYEAR"              -> TaxYearFormatError,
    "INVALID_IDTYPE"               -> DownstreamError,
    "INVALID_CORRELATIONID"        -> DownstreamError,
    "INVALID_INCOMESOURCETYPE"     -> DownstreamError,
    "INVALID_INCOMESOURCEID"       -> BusinessIdFormatError,
    "INCOME_SUBMISSIONS_NOT_EXIST" -> RuleNoIncomeSubmissionsExist,
    "INVALID_ACCOUNTING_PERIOD"    -> DownstreamError,
    "INVALID_QUERY_PARAM"          -> DownstreamError,
    "NOT_FOUND"                    -> NotFoundError,
    "SERVER_ERROR"                 -> DownstreamError,
    "SERVICE_UNAVAILABLE"          -> DownstreamError
  )

  private val tysErrors = Map(
    "INVALID_TAX_YEAR"          -> TaxYearFormatError,
    "INVALID_INCOMESOURCE_ID"   -> BusinessIdFormatError,
    "INVALID_CORRELATION_ID"    -> DownstreamError,
    "INVALID_TAXABLE_ENTITY_ID" -> NinoFormatError,
    "INVALID_INCOME_SOURCETYPE" -> DownstreamError,
    "TAX_YEAR_NOT_SUPPORTED"    -> RuleTaxYearNotSupportedError
  )

  private val downStreamErrors: Map[String, MtdError] = mappingToMtdError ++ tysErrors

}
