/*
 * Copyright 2024 HM Revenue & Customs
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

package v3.retrieveBiss.downstreamErrorMapping

import api.models.domain.TaxYear
import api.models.errors._

import scala.math.Ordered.orderingToOrdered

sealed trait RetrieveBISSDownstreamErrorMapping {
  def errorMap: Map[String, MtdError]
}

object RetrieveBISSDownstreamErrorMapping {

  private val commonErrorsMap: Map[String, MtdError] = Map(
    "INCOME_SUBMISSIONS_NOT_EXIST" -> RuleNoIncomeSubmissionsExist,
    "INVALID_ACCOUNTING_PERIOD"    -> InternalError,
    "INVALID_QUERY_PARAM"          -> InternalError,
    "NOT_FOUND"                    -> NotFoundError,
    "SERVER_ERROR"                 -> InternalError,
    "SERVICE_UNAVAILABLE"          -> InternalError
  )

  case object Api1415 extends RetrieveBISSDownstreamErrorMapping {

    val errorMap: Map[String, MtdError] = commonErrorsMap ++ Map(
      "INVALID_IDVALUE"          -> NinoFormatError,
      "INVALID_TAXYEAR"          -> TaxYearFormatError,
      "INVALID_IDTYPE"           -> InternalError,
      "INVALID_CORRELATIONID"    -> InternalError,
      "INVALID_INCOMESOURCETYPE" -> InternalError,
      "INVALID_INCOMESOURCEID"   -> BusinessIdFormatError
    )

  }

  case object Api1871 extends RetrieveBISSDownstreamErrorMapping {

    val errorMap: Map[String, MtdError] = commonErrorsMap ++ Map(
      "INVALID_TAXABLE_ENTITY_ID" -> NinoFormatError,
      "INVALID_TAX_YEAR"          -> TaxYearFormatError,
      "INVALID_CORRELATION_ID"    -> InternalError,
      "INVALID_INCOMESOURCE_TYPE" -> InternalError,
      "INVALID_INCOMESOURCE_ID"   -> BusinessIdFormatError,
      "TAX_YEAR_NOT_SUPPORTED"    -> RuleTaxYearNotSupportedError
    )

  }

  case object Api1879 extends RetrieveBISSDownstreamErrorMapping {

    private val errorsToRemove: Set[String] = Set(
      "INVALID_INCOMESOURCE_TYPE",
      "INVALID_INCOMESOURCE_ID"
    )

    private val errorMapToAdd: Map[String, MtdError] = Map(
      "INVALID_INCOME_SOURCE_TYPE"       -> InternalError,
      "INVALID_INCOME_SOURCE_ID"         -> BusinessIdFormatError,
      "REQUESTED_TAX_YEAR_NOT_SUPPORTED" -> RuleTaxYearNotSupportedError
    )

    val errorMap: Map[String, MtdError] = Api1871.errorMap -- errorsToRemove ++ errorMapToAdd
  }

  def errorMapFor(taxYear: TaxYear): RetrieveBISSDownstreamErrorMapping = taxYear match {
    case ty if ty >= TaxYear.fromMtd("2025-26")                                     => Api1879
    case ty if ty == TaxYear.fromMtd("2023-24") || ty == TaxYear.fromMtd("2024-25") => Api1871
    case _                                                                          => Api1415
  }

}
