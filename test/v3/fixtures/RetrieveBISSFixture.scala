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

package v3.fixtures

import play.api.libs.json.{JsValue, Json}
import v3.retrieveBiss.def1.model.response.{Loss, Profit, Total}
import v3.retrieveBiss.model.response.{Def1_RetrieveBISSResponse, RetrieveBISSResponse}

trait RetrieveBISSFixture {

  val responseJsonFull: JsValue = Json.parse("""
      |{
      |  "total": {
      |    "income": 1.25,
      |    "expenses": 2.25,
      |    "additions": 5.25,
      |    "deductions": 6.25,
      |    "accountingAdjustments": 7.25
      |  },
      |  "profit": {
      |    "net": 3.25,
      |    "taxable": 8.25
      |  },
      |  "loss": {
      |    "net": 4.25,
      |    "taxable": 9.25
      |  }
      |}
    """.stripMargin)

  val responseJsonMin: JsValue = Json.parse("""
      |{
      |  "total": {
      |    "income": 100.50,
      |    "expenses": 50.5
      |  },
      |  "profit": {
      |    "net": 45.25,
      |    "taxable": 0.50
      |  },
      |  "loss": {
      |    "net": 50,
      |    "taxable": 0.25 
      |  }
      |}
    """.stripMargin)

  val responseFull: RetrieveBISSResponse =
    Def1_RetrieveBISSResponse(
      Total(
        income = 1.25,
        expenses = 2.25,
        additions = Some(5.25),
        deductions = Some(6.25),
        accountingAdjustments = Some(7.25)
      ),
      Profit(
        net = 3.25,
        taxable = 8.25
      ),
      Loss(
        net = 4.25,
        taxable = 9.25
      )
    )

  val responseMin: RetrieveBISSResponse =
    Def1_RetrieveBISSResponse(
      Total(income = 100.50, expenses = 50.5, None, None, None),
      Profit(net = 45.25, taxable = 0.50),
      Loss(net = 50.00, taxable = 0.25)
    )

  val downstreamResponseJsonFull: JsValue = Json.parse("""
      |{
      | "incomeSourceId": "XAIS12345678913",
      | "totalIncome": 1.25,
      | "totalExpenses": 2.25,
      | "netProfit": 3.25,
      | "netLoss": 4.25,
      | "totalAdditions": 5.25,
      | "totalDeductions": 6.25,
      | "accountingAdjustments": 7.25,
      | "taxableProfit": 8.25,
      | "taxableLoss": 9.25
      |}
    """.stripMargin)

  val downstreamResponseJsonMin: JsValue = Json.parse("""
      |{
      | "incomeSourceId": "XAIS12345678913",
      | "totalIncome": 100.5,
      | "totalExpenses": 50.5,
      | "netProfit": 45.25,
      | "netLoss": 50.0,
      | "taxableProfit": 0.5,
      | "taxableLoss": 0.25
      |}
    """.stripMargin)

}
