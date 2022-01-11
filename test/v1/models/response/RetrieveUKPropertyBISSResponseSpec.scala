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

package v1.models.response

import play.api.libs.json.{JsValue, Json}
import support.UnitSpec
import v1.models.response.common.{Loss, Profit, Total}

class RetrieveUKPropertyBISSResponseSpec extends UnitSpec {

  val json: JsValue = Json.parse(
    """
      |{
      |  "total": {
      |    "income": 100.00,
      |    "expenses": 50.00,
      |    "additions": 5.00,
      |    "deductions": 60.00
      |  },
      |  "profit": {
      |    "net": 20.00,
      |    "taxable": 10.00
      |  },
      |  "loss": {
      |    "net": 10.00,
      |    "taxable": 35.00
      |  }
      |}
    """.stripMargin)

  val desJsonFull: JsValue = Json.parse(
    """
      |{
      | "totalIncome": 100.00,
      | "totalExpenses" : 50.00,
      | "totalAdditions" : 5.00,
      | "totalDeductions" : 60.00,
      | "netProfit": 20.00,
      | "taxableProfit" : 10.00,
      | "netLoss": 10.00,
      | "taxableLoss" : 35.00
      |}
    """.stripMargin)

  val desJsonMinimal: JsValue = Json.parse("""
    |{
    | "totalIncome": 100.00,
    | "totalExpenses" : 50.00,
    | "totalAdditions" : 5.00,
    | "totalDeductions" : 60.00
    |}
  """.stripMargin)

  val jsonString: String ="""{"total":{"income":100.00,"expenses":50.00,"additions":5.00,"deductions":60.00},"profit":{"net":20.00,"taxable":10.00},"loss":{"net":10.00,"taxable":35.00}}"""

  val model =
    RetrieveUKPropertyBISSResponse (
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

  val modelMinimal =
    RetrieveUKPropertyBISSResponse (
      Total(
        income = 100.00,
        expenses = Some(50.00),
        additions = Some(5.00),
        deductions = Some(60.00)
      ),
      None,
      None
    )


  "RetrieveUKPropertyBISSResponse" should {

    "write correctly to json" in {
      Json.toJson(model) shouldBe json
    }

    "read correctly from json with full set of data" in {
      desJsonFull.as[RetrieveUKPropertyBISSResponse] shouldBe model
    }
    "read correctly from json with only mandatory set of data" in {
      desJsonMinimal.as[RetrieveUKPropertyBISSResponse] shouldBe modelMinimal
    }


    "toJsonString" in {
      model.toJsonString shouldBe jsonString
    }
  }
}
