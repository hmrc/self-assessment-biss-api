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

package fixtures

import play.api.libs.json.{JsValue, Json}
import v1.models.response.RetrieveSelfEmploymentBISSResponse
import v1.models.response.common.{Loss, Profit, Total}

object RetrieveSelfEmploymentBISSFixture {

  val mtdResponse: JsValue = Json.parse(
    """
      |{
      |  "total": {
      |    "income": 100.00,
      |    "expenses": 50.00,
      |    "additions": 5.00,
      |    "deductions": 60.00
      |  },
      |  "accountingAdjustments": -30.00,
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

  val mtdResponseWithOnlyRequiredData: JsValue = Json.parse(
    """
      |{
      |  "total": {
      |    "income": 100.00,
      |    "expenses": 50.00,
      |    "additions": 5.00,
      |    "deductions": 60.00
      |  }
      |}
    """.stripMargin)

  val responseObj =
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

  val responseObjWithOnlyRequiredData =
    RetrieveSelfEmploymentBISSResponse (
      Total(
        income = 100.00,
        expenses = Some(50.00),
        additions = Some(5.00),
        deductions = Some(60.00)
      ),
      None, None, None
    )

  val desResponse: JsValue = Json.parse(
    """
      |{
      | "totalIncome": 100.00,
      | "totalExpenses" : 50.00,
      | "totalAdditions" : 5.00,
      | "totalDeductions" : 60.00,
      | "netProfit": 20.00,
      | "taxableProfit" : 10.00,
      | "netLoss": 10.00,
      | "taxableLoss" : 35.00,
      | "accountingAdjustments": -30.00
      |}
    """.stripMargin)

  val desResponseWithOnlyRequiredData: JsValue = Json.parse(
    """
      |{
      | "totalIncome": 100.00,
      | "totalExpenses" : 50.00,
      | "totalAdditions" : 5.00,
      | "totalDeductions" : 60.00
      |}
    """.stripMargin)
}
