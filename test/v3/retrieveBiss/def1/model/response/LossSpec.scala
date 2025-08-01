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

package v3.retrieveBiss.def1.model.response

import play.api.libs.json.{JsValue, Json}
import support.UnitSpec

class LossSpec extends UnitSpec {

  private val json: JsValue = Json.parse(
    """
      |{
      |  "net": 0.00,
      |  "taxable": 35.00
      |}
    """.stripMargin
  )

  private val downstreamJson: JsValue = Json.parse(
    """
      |{
      |  "netLoss": 0,
      |  "taxableLoss": 35.00
      |}
    """.stripMargin
  )

  private val model: Loss = Loss(net = 0, taxable = 35.00)

  "Loss" should {

    "write correctly to json" in {
      Json.toJson(model) shouldBe json
    }

    "read correctly from a json" in {
      downstreamJson.as[Loss] shouldBe model
    }
  }

}
