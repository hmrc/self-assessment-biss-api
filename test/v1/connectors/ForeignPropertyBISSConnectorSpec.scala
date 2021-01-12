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

package v1.connectors

import mocks.MockAppConfig
import uk.gov.hmrc.domain.Nino
import v1.mocks.MockHttpClient
import v1.models.des.IncomeSourceType
import v1.models.outcomes.ResponseWrapper
import v1.models.requestData.{DesTaxYear, RetrieveForeignPropertyBISSRequest}
import v1.models.response.RetrieveForeignPropertyBISSResponse
import v1.models.response.common.{Loss, Profit, Total}

import scala.concurrent.Future

class ForeignPropertyBISSConnectorSpec extends ConnectorSpec {

  val desTaxYear: DesTaxYear = DesTaxYear("2019")
  val nino: Nino = Nino("AA123456A")
  val incomeSourceId: String = "041f7e4d-87b9-4d4a-a296-3cfbdf92f7e2"
  val businessId: String = "XAIS12345678910"

  val response: RetrieveForeignPropertyBISSResponse = RetrieveForeignPropertyBISSResponse(
    Total(
      income = 100.00,
      expenses = Some(50.00),
      additions = Some(5.00),
      deductions = Some(60.00)
    ),
    Some(Profit(
      net = Some(20.00),
      taxable = Some(10.00)
    )),
    Some(Loss(
      net = Some(10.00),
      taxable = Some(35.00)
    ))
  )

  val responseWithMissingOptionals: RetrieveForeignPropertyBISSResponse = RetrieveForeignPropertyBISSResponse(
    Total(
      income = 100.00,
      expenses = Some(50.00),
      additions = Some(5.00),
      deductions = Some(60.00)
    ),
    None, None
  )

  class Test extends MockHttpClient with MockAppConfig {
    val connector: ForeignPropertyBISSConnector = new ForeignPropertyBISSConnector(http = mockHttpClient, appConfig = mockAppConfig)

    val desRequestHeaders = Seq("Environment" -> "des-environment", "Authorization" -> s"Bearer des-token")
    MockedAppConfig.desBaseUrl returns baseUrl
    MockedAppConfig.desToken returns "des-token"
    MockedAppConfig.desEnvironment returns "des-environment"
  }

  "retrieveBiss" when {
    val request = RetrieveForeignPropertyBISSRequest(nino, businessId, IncomeSourceType.`foreign-property`, desTaxYear)

    "a valid request is supplied" should {
      "return a successful response with the correct correlationId" in new Test {

        val expected = Right(ResponseWrapper(correlationId, response))

        MockedHttpClient
          .get(s"$baseUrl/income-tax/income-sources/nino/$nino/foreign-property/${desTaxYear.toString}/biss?incomesourceid=$businessId", desRequestHeaders: _*)
          .returns(Future.successful(expected))

        await(connector.retrieveBiss(request)) shouldBe expected
      }
    }

    "a valid request is supplied with missing optionals" should {
      "return a successful response with the correct correlationId" in new Test {
        val expected = Right(ResponseWrapper(correlationId, responseWithMissingOptionals))

        MockedHttpClient
          .get(s"$baseUrl/income-tax/income-sources/nino/$nino/foreign-property/${desTaxYear.toString}/biss?incomesourceid=$businessId", desRequestHeaders: _*)
          .returns(Future.successful(expected))

        await(connector.retrieveBiss(request)) shouldBe expected
      }
    }
  }
}