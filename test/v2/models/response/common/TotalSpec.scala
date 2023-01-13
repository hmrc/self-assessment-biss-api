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

import play.api.libs.json.{JsValue, Json}
import support.UnitSpec

class TotalSpec extends UnitSpec {

  val json: JsValue = Json.parse("""
      |{
      |        "income": 100.00,
      |        "expenses": 50.00,
      |        "additions": 5.00,
      |        "deductions": 60.00,
      |        "accountingAdjustments": -30.00
      |}
      |""".stripMargin)

  val desJson: JsValue = Json.parse("""
      |{
      |    "incomeSourceID": "string",
      |    "totalIncome": 100.00,
      |    "totalExpenses": 50.00,
      |    "totalAdditions": 5.00,
      |    "totalDeductions": 60.00,
      |    "accountingAdjustments": -30.00
      |}
      |""".stripMargin)

  val minDesJson: JsValue = Json.parse("""
      |{
      |    "totalIncome": 100.00
      |}
      |""".stripMargin)

  val minJson: JsValue = Json.parse("""
      |{
      |    "income": 100.00
      |}
      |""".stripMargin)

  val model: Total = Total(
    100.00,
    Some(50.00),
    Some(5.00),
    Some(60.00),
    Some(-30.00)
  )

  val minModel: Total = Total(
    100.00,
    None,
    None,
    None,
    None
  )

  "Total" should {

    "write correctly to json" in {
      Json.toJson(model) shouldBe json
    }
    "write correctly to a minimal json" in {
      Json.toJson(minModel) shouldBe minJson
    }

    "read correctly from a json" in {
      desJson.as[Total] shouldBe model
    }

    "read correctly from a minimal json" in {
      minDesJson.as[Total] shouldBe minModel
    }
  }

}
