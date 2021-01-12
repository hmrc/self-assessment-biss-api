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

package v1.models.response.common

import play.api.libs.json.{JsValue, Json}
import support.UnitSpec

class TotalSpec extends UnitSpec {

  val json: JsValue = Json.parse(
    """
      |{
      | "income": 100.00,
      | "additions" : 100.00,
      | "deductions" : 100.25,
      | "expenses" : 100.25
      |}
    """.stripMargin)

  val desJson: JsValue = Json.parse(
    """
      |{
      | "totalIncome": 100.00,
      | "totalAdditions" : 100.00,
      | "totalDeductions" : 100.25,
      | "totalExpenses" : 100.25
      |}
    """.stripMargin)

  val model =
    Total(
      income = 100.00,
      additions = Some(100.00),
      deductions = Some(100.25),
      expenses = Some(100.25)
    )

  val mtdJsonWithNegativeValues: JsValue = Json.parse(
    """
      |{
      | "income": 100.00,
      | "additions" : -100.00,
      | "deductions" : 100.25,
      | "expenses" : -100.25
      |}
    """.stripMargin)

  val desJsonWithNegativeValues: JsValue = Json.parse(
    """
      |{
      | "totalIncome": 100.00,
      | "totalAdditions" : -100.00,
      | "totalDeductions" : 100.25,
      | "totalExpenses" : -100.25
      |}
    """.stripMargin)

  val modelWithNegativeValues =
    Total(
      income = 100.00,
      additions = Some(-100.00),
      deductions = Some(100.25),
      expenses = Some(-100.25)
    )

  "TaxBand" should {

    "write correctly to json" in {
      Json.toJson(model) shouldBe json
    }
    "write correctly to json with negative values" in {
      Json.toJson(modelWithNegativeValues) shouldBe mtdJsonWithNegativeValues
    }

    "read correctly from json" in {
      desJson.as[Total] shouldBe model
    }

    "read correctly from json having negative values" in {
      desJson.as[Total] shouldBe model
    }
  }
}
