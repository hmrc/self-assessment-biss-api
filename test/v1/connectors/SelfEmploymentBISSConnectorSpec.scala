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

package v1.connectors

import fixtures.RetrieveSelfEmploymentBISSFixture._
import mocks.MockAppConfig
import v1.mocks.MockHttpClient
import v1.models.domain.Nino
import v1.models.outcomes.ResponseWrapper
import v1.models.requestData.{DesTaxYear, RetrieveSelfEmploymentBISSRequest}

import scala.concurrent.Future

class SelfEmploymentBISSConnectorSpec extends ConnectorSpec {

  val desTaxYear: DesTaxYear = DesTaxYear("2019")
  val nino: String = "AA123456A"
  val incomeSourceId: String = "041f7e4d-87b9-4d4a-a296-3cfbdf92f7e2"

  class Test extends MockHttpClient with MockAppConfig {
    val connector: SelfEmploymentBISSConnector = new SelfEmploymentBISSConnector(http = mockHttpClient, appConfig = mockAppConfig)

    val desRequestHeaders = Seq("Environment" -> "des-environment", "Authorization" -> s"Bearer des-token")
    MockedAppConfig.desBaseUrl returns baseUrl
    MockedAppConfig.desToken returns "des-token"
    MockedAppConfig.desEnvironment returns "des-environment"
    MockedAppConfig.desEnvironmentHeaders returns Some(allowedDesHeaders)
  }

  "retrieveBiss" when {

    val request = RetrieveSelfEmploymentBISSRequest(Nino(nino), desTaxYear, incomeSourceId)

    "a valid request is supplied" should {
      "return a successful response with the correct correlationId" in new Test {

        val expected = Right(ResponseWrapper(correlationId, responseObj))

        MockedHttpClient
          .parameterGet(s"$baseUrl/income-tax/income-sources/nino/$nino/self-employment/${desTaxYear.toString}/biss",
            dummyDesHeaderCarrierConfig,
            Seq(("incomesourceid", incomeSourceId)),
            desRequestHeaders,
            Seq("AnotherHeader" -> "HeaderValue"))
          .returns(Future.successful(expected))

        await(connector.retrieveBiss(request)) shouldBe expected

      }

      "des return valid response with only mandatory fields and correlationId" in new Test {

        val expected = Right(ResponseWrapper(correlationId, responseObjWithOnlyRequiredData))

        MockedHttpClient
          .parameterGet(s"$baseUrl/income-tax/income-sources/nino/$nino/self-employment/${desTaxYear.toString}/biss",
            dummyDesHeaderCarrierConfig,
            Seq(("incomesourceid", incomeSourceId)))
          .returns(Future.successful(expected))

        await(connector.retrieveBiss(request)) shouldBe expected

      }
    }
  }
}