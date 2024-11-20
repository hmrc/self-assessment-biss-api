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

package v3.retrieveBiss.downstreamUriBuilder

import api.connectors.DownstreamUri
import api.models.des.IncomeSourceType
import api.models.domain.{BusinessId, Nino, TaxYear, TaxYearPropertyCheckSupport}
import org.scalatestplus.scalacheck.ScalaCheckDrivenPropertyChecks
import support.UnitSpec
import v3.retrieveBiss.downstreamUriBuilder.RetrieveBISSDownstreamUriBuilder._
import v3.retrieveBiss.model.response.RetrieveBISSResponse

class RetrieveBISSDownstreamUriBuilderSpec extends UnitSpec with ScalaCheckDrivenPropertyChecks with TaxYearPropertyCheckSupport {

  private val nino: Nino = Nino("AA123456A")
  private val businessId: BusinessId = BusinessId("XAIS12345678910")
  private val incomeSourceType: IncomeSourceType = IncomeSourceType.`self-employment`

  "RetrieveBISSDownstreamUriBuilder" should {
    "return correct URI builder, URI and query parameter for Non TYS tax years" in {
      forPreTysTaxYears { taxYear =>
        val downstreamUriBuilder: RetrieveBISSDownstreamUriBuilder[RetrieveBISSResponse] =
          RetrieveBISSDownstreamUriBuilder.downstreamUriFor[RetrieveBISSResponse](taxYear)

        downstreamUriBuilder shouldBe Api1415

        val (uri, queryParams): (DownstreamUri[RetrieveBISSResponse], Seq[(String, String)]) =
          downstreamUriBuilder.buildUri(nino, businessId, incomeSourceType, taxYear)

        uri.value shouldBe s"income-tax/income-sources/nino/$nino/$incomeSourceType/${taxYear.asDownstream}/biss"
        queryParams shouldBe Seq("incomeSourceId" -> s"$businessId")
      }
    }

    "return correct URI builder, URI and no query parameter for TYS tax years 2023-24 and 2024-25" in {
      forTaxYearsInRange(TaxYear.fromMtd("2023-24"), TaxYear.fromMtd("2024-25")) { taxYear =>
        val downstreamUriBuilder: RetrieveBISSDownstreamUriBuilder[RetrieveBISSResponse] =
          RetrieveBISSDownstreamUriBuilder.downstreamUriFor[RetrieveBISSResponse](taxYear)

        downstreamUriBuilder shouldBe Api1871

        val (uri, queryParams): (DownstreamUri[RetrieveBISSResponse], Seq[(String, String)]) =
          downstreamUriBuilder.buildUri(nino, businessId, incomeSourceType, taxYear)

        uri.value shouldBe s"income-tax/income-sources/${taxYear.asTysDownstream}/$nino/$businessId/$incomeSourceType/biss"
        queryParams shouldBe Nil
      }
    }

    "return correct URI builder, URI and no query parameter for TYS tax years 2025-26 onwards" in {
      forTaxYearsFrom(TaxYear.fromMtd("2025-26")) { taxYear =>
        val downstreamUriBuilder: RetrieveBISSDownstreamUriBuilder[RetrieveBISSResponse] =
          RetrieveBISSDownstreamUriBuilder.downstreamUriFor[RetrieveBISSResponse](taxYear)

        downstreamUriBuilder shouldBe Api1879

        val (uri, queryParams): (DownstreamUri[RetrieveBISSResponse], Seq[(String, String)]) =
          downstreamUriBuilder.buildUri(nino, businessId, incomeSourceType, taxYear)

        uri.value shouldBe s"income-tax/${taxYear.asTysDownstream}/income-sources/$nino/$businessId/$incomeSourceType/biss"
        queryParams shouldBe Nil
      }
    }
  }
}
