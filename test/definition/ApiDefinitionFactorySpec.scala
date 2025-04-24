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

package definition

import cats.implicits.catsSyntaxValidatedId
import config.Deprecation.NotDeprecated
import config.MockAppConfig
import definition.APIStatus._
import routing.{Version2, Version3}
import support.UnitSpec

class ApiDefinitionFactorySpec extends UnitSpec {

  class Test extends MockAppConfig {
    val apiDefinitionFactory = new ApiDefinitionFactory(mockAppConfig)
    MockedAppConfig.apiGatewayContext returns "api.gateway.context"
  }

  "definition" when {
    "called" should {
      "return a valid Definition case class with endpoint version settings taken from configuration" in new Test {
        List(Version2, Version3).foreach { version =>
          MockedAppConfig.apiStatus(version) returns "ALPHA"
          MockedAppConfig.endpointsEnabled(version) returns false
          MockedAppConfig.deprecationFor(version).returns(NotDeprecated.valid).anyNumberOfTimes()
        }

        apiDefinitionFactory.definition shouldBe
          Definition(
            api = APIDefinition(
              name = "Business Income Source Summary (MTD)",
              description = "An API for providing Business Income Source Summary data",
              context = "api.gateway.context",
              categories = Seq("INCOME_TAX_MTD"),
              versions = Seq(
                APIVersion(
                  version = Version2,
                  status = ALPHA,
                  endpointsEnabled = false
                ),
                APIVersion(
                  version = Version3,
                  status = ALPHA,
                  endpointsEnabled = false
                )
              ),
              requiresTrust = None
            )
          )
      }
    }
  }

  "buildAPIStatus" when {
    "the 'apiStatus' parameter is present and valid" should {
      "return the correct status" in new Test {
        MockedAppConfig.apiStatus(Version2) returns "BETA"
        MockedAppConfig.deprecationFor(Version2).returns(NotDeprecated.valid).anyNumberOfTimes()
        apiDefinitionFactory.buildAPIStatus(version = Version2) shouldBe BETA
      }
    }

    "the 'apiStatus' parameter is present and invalid" should {
      "default to alpha" in new Test {
        MockedAppConfig.apiStatus(Version2) returns "ALPHO"
        MockedAppConfig.deprecationFor(Version2).returns(NotDeprecated.valid).anyNumberOfTimes()
        apiDefinitionFactory.buildAPIStatus(version = Version2) shouldBe ALPHA
      }
    }
  }

}
