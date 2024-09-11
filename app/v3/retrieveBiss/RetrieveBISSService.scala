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

package v3.retrieveBiss

import api.controllers.RequestContext
import api.models.errors._
import api.services.{BaseService, ServiceOutcome}
import cats.implicits._
import v3.retrieveBiss.model.request.RetrieveBISSRequestData
import v3.retrieveBiss.model.response.RetrieveBISSResponse

import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class RetrieveBISSService @Inject() (connector: RetrieveBISSConnector) extends BaseService {

  def retrieveBiss(
      request: RetrieveBISSRequestData)(implicit ctx: RequestContext, ec: ExecutionContext): Future[ServiceOutcome[RetrieveBISSResponse]] = {

    connector
      .retrieveBiss(request)
      .map(_.leftMap(mapDownstreamErrors(downstreamErrorMap)))
  }

  private val downstreamErrorMap: Map[String, MtdError] = {
    val errors = Map(
      "INVALID_IDVALUE"              -> NinoFormatError,
      "INVALID_TAXYEAR"              -> TaxYearFormatError,
      "INVALID_IDTYPE"               -> InternalError,
      "INVALID_CORRELATIONID"        -> InternalError,
      "INVALID_INCOMESOURCETYPE"     -> InternalError,
      "INVALID_INCOMESOURCEID"       -> BusinessIdFormatError,
      "INCOME_SUBMISSIONS_NOT_EXIST" -> RuleNoIncomeSubmissionsExist,
      "INVALID_ACCOUNTING_PERIOD"    -> InternalError,
      "INVALID_QUERY_PARAM"          -> InternalError,
      "NOT_FOUND"                    -> NotFoundError,
      "SERVER_ERROR"                 -> InternalError,
      "SERVICE_UNAVAILABLE"          -> InternalError
    )

    val extraTysErrors = Map(
      "INVALID_TAX_YEAR"          -> TaxYearFormatError,
      "INVALID_INCOMESOURCE_ID"   -> BusinessIdFormatError,
      "INVALID_CORRELATION_ID"    -> InternalError,
      "INVALID_TAXABLE_ENTITY_ID" -> NinoFormatError,
      "INVALID_INCOME_SOURCETYPE" -> InternalError,
      "TAX_YEAR_NOT_SUPPORTED"    -> RuleTaxYearNotSupportedError
    )

    errors ++ extraTysErrors
  }

}
