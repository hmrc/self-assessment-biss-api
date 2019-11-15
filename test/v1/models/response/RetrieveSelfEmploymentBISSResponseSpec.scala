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

package v1.models.response

import fixtures.RetrieveSelfEmploymentBISSFixture._
import play.api.libs.json.Json
import support.UnitSpec
import v1.models.response.common.{Loss, Profit, Total}

class RetrieveSelfEmploymentBISSResponseSpec extends UnitSpec {

  val jsonString: String ="""{"total":{"income":100.00,"expenses":50.00,"additions":5.00,"deductions":60.00},"accountingAdjustments":-30.00,"profit":{"net":20.00,"taxable":10.00},"loss":{"net":10.00,"taxable":35.00}}"""

  "RetrieveSelfEmploymentBISSResponse" should {

    "write correctly to json" in {
      Json.toJson(responseObj) shouldBe mtdResponse
    }

    "read correctly from json" in {
      desResponse.as[RetrieveSelfEmploymentBISSResponse] shouldBe responseObj
    }

    "toJsonString" in {
      responseObj.toJsonString shouldBe jsonString
    }
  }
}
