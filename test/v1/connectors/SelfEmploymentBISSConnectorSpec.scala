/*
 * Copyright 2019 HM Revenue & Customs
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
import v1.models.outcomes.ResponseWrapper
import v1.models.requestData.{DesTaxYear, RetrieveSelfEmploymentBISSRequest}
import v1.models.response.selfEmployment.{Loss, Profit, RetrieveSelfEmploymentBISSResponse, Total}

import scala.concurrent.Future

class SelfEmploymentBISSConnectorSpec extends ConnectorSpec {

  val desTaxYear = DesTaxYear("2019")
  val nino = Nino("AA123456A")
  val incomeSourceId = "041f7e4d-87b9-4d4a-a296-3cfbdf92f7e2"

  val response =
    RetrieveSelfEmploymentBISSResponse (
      Total(
        income = 100.00,
        expenses = Some(50.00),
        additions = Some(5.00),
        deductions = Some(60.00)
      ),
      accountingAdjustments = Some(-30.00),
      Some(Profit(
        net = Some(20.00),
        taxable = Some(10.00)
      )),
      Some(Loss(
        net = Some(10.00),
        taxable = Some(35.00)
      ))
    )

  class Test extends MockHttpClient with MockAppConfig {
    val connector: SelfEmploymentBISSConnector = new SelfEmploymentBISSConnector(http = mockHttpClient, appConfig = mockAppConfig)

    val desRequestHeaders = Seq("Environment" -> "des-environment", "Authorization" -> s"Bearer des-token")
    MockedAppConfig.desBaseUrl returns baseUrl
    MockedAppConfig.desToken returns "des-token"
    MockedAppConfig.desEnvironment returns "des-environment"
  }

  "retrieveBiss" when {

    val request = RetrieveSelfEmploymentBISSRequest(nino, desTaxYear, incomeSourceId)

    "a valid request is supplied" should {
      "return a successful response with the correct correlationId" in new Test {

        val expected = Right(ResponseWrapper(correlationId, response))

        MockedHttpClient
          .parameterGet(s"$baseUrl/income-tax/income-sources/nino/$nino/self-employment/${desTaxYear.toString}/biss", Seq(("incomesourceid", incomeSourceId)), desRequestHeaders: _*)
          .returns(Future.successful(expected))

        await(connector.retrieveBiss(request)) shouldBe expected

      }
    }
  }
}
