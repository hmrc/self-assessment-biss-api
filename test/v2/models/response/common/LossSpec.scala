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

package v2.models.response.common

import support.UnitSpec
import play.api.libs.json.Json

class LossSpec extends UnitSpec {

  val json = Json.parse("""
      |{
      |        "net": 0.00,
      |        "taxable": 35.00
      |}
      |""".stripMargin)

  val desJson = Json.parse("""
      |{
      |    "netLoss": 0,
      |    "taxableLoss": 35.00
      |}
      |""".stripMargin)

  val model =
    Loss(
      Some(0),
      Some(35.00)
    )

  "Loss" should {

    "write correctly to json" in {
      Json.toJson(model) shouldBe json
    }

    "read correctly from a json" in {
      desJson.as[Loss] shouldBe model
    }
  }

}
