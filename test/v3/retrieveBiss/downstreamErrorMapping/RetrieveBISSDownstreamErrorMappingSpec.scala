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

import api.models.domain.{TaxYear, TaxYearPropertyCheckSupport}
import org.scalatestplus.scalacheck.ScalaCheckDrivenPropertyChecks
import support.UnitSpec
import v3.retrieveBiss.downstreamErrorMapping.RetrieveBISSDownstreamErrorMapping._

class RetrieveBISSDownstreamErrorMappingSpec extends UnitSpec with ScalaCheckDrivenPropertyChecks with TaxYearPropertyCheckSupport {

  "RetrieveBISSDownstreamErrorMapping" should {
    "return correct downstream error mapping for Non TYS tax years" in {
      forPreTysTaxYears { taxYear =>
        val downstreamErrorMapping: RetrieveBISSDownstreamErrorMapping = errorMapFor(taxYear)

        downstreamErrorMapping shouldBe Api1415
        downstreamErrorMapping.errorMap.keys should contain.allOf(
          "INVALID_IDVALUE",
          "INVALID_TAXYEAR",
          "INVALID_IDTYPE",
          "INVALID_CORRELATIONID",
          "INVALID_INCOMESOURCETYPE",
          "INVALID_INCOMESOURCEID",
          "INCOME_SUBMISSIONS_NOT_EXIST",
          "INVALID_ACCOUNTING_PERIOD",
          "INVALID_QUERY_PARAM",
          "NOT_FOUND",
          "SERVER_ERROR",
          "SERVICE_UNAVAILABLE"
        )
      }
    }

    "return correct downstream error mapping for TYS tax years 2023-24 and 2024-25" in {
      forTaxYearsInRange(TaxYear.fromMtd("2023-24"), TaxYear.fromMtd("2024-25")) { taxYear =>
        val downstreamErrorMapping: RetrieveBISSDownstreamErrorMapping = errorMapFor(taxYear)

        downstreamErrorMapping shouldBe Api1871
        downstreamErrorMapping.errorMap.keys should contain.allOf(
          "INVALID_TAXABLE_ENTITY_ID",
          "INVALID_TAX_YEAR",
          "INVALID_CORRELATION_ID",
          "INVALID_INCOMESOURCE_TYPE",
          "INVALID_INCOMESOURCE_ID",
          "INCOME_SUBMISSIONS_NOT_EXIST",
          "INVALID_ACCOUNTING_PERIOD",
          "INVALID_QUERY_PARAM",
          "TAX_YEAR_NOT_SUPPORTED",
          "NOT_FOUND",
          "SERVER_ERROR",
          "SERVICE_UNAVAILABLE"
        )
      }
    }

    "return correct downstream error mapping for TYS tax years 2025-26 onwards" in {
      forTaxYearsFrom(TaxYear.fromMtd("2025-26")) { taxYear =>
        val downstreamErrorMapping: RetrieveBISSDownstreamErrorMapping = errorMapFor(taxYear)

        downstreamErrorMapping shouldBe Api1879
        downstreamErrorMapping.errorMap.keys should contain.allOf(
          "INVALID_TAXABLE_ENTITY_ID",
          "INVALID_TAX_YEAR",
          "INVALID_CORRELATION_ID",
          "INVALID_INCOME_SOURCE_TYPE",
          "INVALID_INCOME_SOURCE_ID",
          "INCOME_SUBMISSIONS_NOT_EXIST",
          "INVALID_ACCOUNTING_PERIOD",
          "INVALID_QUERY_PARAM",
          "TAX_YEAR_NOT_SUPPORTED",
          "REQUESTED_TAX_YEAR_NOT_SUPPORTED",
          "NOT_FOUND",
          "SERVER_ERROR",
          "SERVICE_UNAVAILABLE"
        )

        val excludedKeys: Set[String] = Set("INVALID_INCOMESOURCE_TYPE", "INVALID_INCOMESOURCE_ID")

        excludedKeys.foreach { key =>
          downstreamErrorMapping.errorMap.keys should not contain key
        }
      }
    }
  }

}
