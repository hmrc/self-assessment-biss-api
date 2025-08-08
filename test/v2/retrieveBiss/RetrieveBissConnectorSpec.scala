/*
 * Copyright 2025 HM Revenue & Customs
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

import api.connectors.ConnectorSpec
import api.models.domain.*
import api.models.outcomes.ResponseWrapper
import play.api.Configuration
import uk.gov.hmrc.http.StringContextOps
import v2.retrieveBiss.def1.model.response.*
import v2.retrieveBiss.model.domain.TypeOfBusiness
import v2.retrieveBiss.model.request.{Def1_RetrieveBissRequestData, RetrieveBissRequestData}
import v2.retrieveBiss.model.response.{Def1_RetrieveBissResponse, RetrieveBissResponse}

import scala.concurrent.Future

class RetrieveBissConnectorSpec extends ConnectorSpec {

  val taxYearMtd        = "2018-19"
  val taxYearDownstream = "2019"
  val taxYearTys        = "2023-24"
  val nino              = "AA123456A"
  val businessId        = "businessId"

  // WLOG
  val response: RetrieveBissResponse = Def1_RetrieveBissResponse(Total(100.00, 50.0, None, None, None), Profit(0, 0), Loss(100.0, 0.0))

  "retrieveBiss" should {
    "make a request to downstream as per the specification" when {
      withBusinessType(TypeOfBusiness.`uk-property-non-fhl`, "uk-property")
      withBusinessType(TypeOfBusiness.`uk-property-fhl`, "fhl-property-uk")
      withBusinessType(TypeOfBusiness.`self-employment`, "self-employment")
      withBusinessType(TypeOfBusiness.`foreign-property-fhl-eea`, "fhl-property-eea")
      withBusinessType(TypeOfBusiness.`foreign-property`, "foreign-property")

      def withBusinessType(typeOfBusiness: TypeOfBusiness, incomeSourceTypePathParam: String): Unit = {
        s"businessType is $typeOfBusiness and non TYS" in new IfsTest with Test {
          val expectedUrl = url"$baseUrl/income-tax/income-sources/nino/$nino/$incomeSourceTypePathParam/$taxYearDownstream/biss"

          val request: RetrieveBissRequestData =
            Def1_RetrieveBissRequestData(Nino(nino), typeOfBusiness, TaxYear.fromMtd(taxYearMtd), BusinessId(businessId))

          val expected: Right[Nothing, ResponseWrapper[RetrieveBissResponse]] = Right(ResponseWrapper(correlationId, response))

          willGet(url = expectedUrl, parameters = Seq("incomeSourceId" -> businessId)) returns Future.successful(expected)

          await(connector.retrieveBiss(request)).shouldBe(expected)
        }

        s"businessType is $typeOfBusiness and TYS (IFS enabled)" in new IfsTest with Test {
          val expectedUrl = url"$baseUrl/income-tax/income-sources/23-24/$nino/$businessId/$incomeSourceTypePathParam/biss"
          val request: RetrieveBissRequestData =
            Def1_RetrieveBissRequestData(Nino(nino), typeOfBusiness, TaxYear.fromMtd(taxYearTys), BusinessId(businessId))

          MockedAppConfig.featureSwitches.returns(Configuration("ifs_hip_migration_1871.enabled" -> false))

          val expected: Right[Nothing, ResponseWrapper[RetrieveBissResponse]] = Right(ResponseWrapper(correlationId, response))

          willGet(url = expectedUrl) returns Future.successful(expected)

          await(connector.retrieveBiss(request)).shouldBe(expected)
        }

        s"businessType is $typeOfBusiness and TYS (HIP enabled)" in new HipTest with Test {
          val expectedUrl = url"$baseUrl/itsa/income-tax/v1/income-sources/23-24/$nino/$businessId/$incomeSourceTypePathParam/biss"
          val request: RetrieveBissRequestData =
            Def1_RetrieveBissRequestData(Nino(nino), typeOfBusiness, TaxYear.fromMtd(taxYearTys), BusinessId(businessId))

          MockedAppConfig.featureSwitches.returns(Configuration("ifs_hip_migration_1871.enabled" -> true))

          val expected: Right[Nothing, ResponseWrapper[RetrieveBissResponse]] = Right(ResponseWrapper(correlationId, response))

          willGet(url = expectedUrl) returns Future.successful(expected)

          await(connector.retrieveBiss(request)).shouldBe(expected)
        }
      }
    }
  }

  trait Test {
    self: ConnectorTest =>
    val connector: RetrieveBissConnector = new RetrieveBissConnector(http = mockHttpClient, appConfig = mockAppConfig)

  }

}
