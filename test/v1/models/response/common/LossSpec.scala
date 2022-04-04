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

package v1.models.response.common

import play.api.libs.json.{JsSuccess, JsValue, Json}
import support.UnitSpec

class LossSpec extends UnitSpec {

  val json: JsValue = Json.parse("""
      |{
      | "net": 100.00,
      | "taxable" : 50.00
      |}
    """.stripMargin)

  val desJson: JsValue = Json.parse("""
      |{
      | "netLoss": 100.00,
      | "taxableLoss" : 50.00
      |}
    """.stripMargin)

  val model =
    Loss(
      net = Some(100.00),
      taxable = Some(50.00)
    )

  "TaxBand" should {

    "write correctly to json" in {
      Json.toJson(model) shouldBe json
    }

    "read correctly from json" in {
      desJson.validate[Loss] shouldBe JsSuccess(model)
    }
  }

}
