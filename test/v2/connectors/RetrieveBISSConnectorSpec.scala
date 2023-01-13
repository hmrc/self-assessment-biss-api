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

package v2.connectors

import v2.models.domain.{Nino, TypeOfBusiness}
import v2.models.outcomes.ResponseWrapper
import v2.models.requestData.{RetrieveBISSRequest, TaxYear}
import v2.models.response.RetrieveBISSResponse
import v2.models.response.common.Total

import scala.concurrent.Future

class RetrieveBISSConnectorSpec extends ConnectorSpec {

  val taxYearMtd        = "2018-19"
  val taxYearDownstream = "2019"
  val taxYearTys        = "2023-24"
  val nino              = "AA123456A"
  val businessId        = "businessId"

  // WLOG
  val response: RetrieveBISSResponse = RetrieveBISSResponse(Total(100.00, None, None, None, None), None, None)

  trait Test {
    _: ConnectorTest =>
    val connector: RetrieveBISSConnector = new RetrieveBISSConnector(http = mockHttpClient, appConfig = mockAppConfig)
  }

  "retrieveBiss" should {
    "make a request to downstream as per the specification" when {
      withBusinessType(TypeOfBusiness.`uk-property-non-fhl`, "uk-property")
      withBusinessType(TypeOfBusiness.`uk-property-fhl`, "fhl-property-uk")
      withBusinessType(TypeOfBusiness.`self-employment`, "self-employment")
      withBusinessType(TypeOfBusiness.`foreign-property-fhl-eea`, "fhl-property-eea")
      withBusinessType(TypeOfBusiness.`foreign-property`, "foreign-property")

      def withBusinessType(typeOfBusiness: TypeOfBusiness, incomeSourceTypePathParam: String): Unit = {
        s"businessType is $typeOfBusiness and non TYS" in new IfsTest with Test {
          val expectedUrl = s"$baseUrl/income-tax/income-sources/nino/$nino/$incomeSourceTypePathParam/$taxYearDownstream/biss"

          val request: RetrieveBISSRequest = RetrieveBISSRequest(Nino(nino), typeOfBusiness, TaxYear.fromMtd(taxYearMtd), businessId)

          val expected: Right[Nothing, ResponseWrapper[RetrieveBISSResponse]] = Right(ResponseWrapper(correlationId, response))

          willGet(url = expectedUrl, parameters = Seq("incomeSourceId" -> businessId)) returns Future.successful(expected)

          await(connector.retrieveBiss(request)) shouldBe expected
        }

        s"businessType is $typeOfBusiness and TYS" in new TysIfsTest with Test {
          val expectedUrl                  = s"$baseUrl/income-tax/income-sources/23-24/$nino/$businessId/$incomeSourceTypePathParam/biss"
          val request: RetrieveBISSRequest = RetrieveBISSRequest(Nino(nino), typeOfBusiness, TaxYear.fromMtd(taxYearTys), businessId)

          val expected: Right[Nothing, ResponseWrapper[RetrieveBISSResponse]] = Right(ResponseWrapper(correlationId, response))

          willGet(url = expectedUrl) returns Future.successful(expected)

          await(connector.retrieveBiss(request)) shouldBe expected
        }
      }
    }
  }

}
