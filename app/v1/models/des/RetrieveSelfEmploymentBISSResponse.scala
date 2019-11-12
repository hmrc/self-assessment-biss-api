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

package v1.models.des

import play.api.libs.json._
import org.json4s.JsonAST._
import org.json4s.native.Serialization
import org.json4s.{CustomSerializer, DefaultFormats, Formats}

case class RetrieveSelfEmploymentDesResponse(totalIncome: BigDecimal,
                                             totalExpenses: Option[BigDecimal],
                                             totalAdditions: Option[BigDecimal],
                                             totalDeductions: Option[BigDecimal],
                                             accountingAdjustments: Option[BigDecimal],
                                             netProfit: Option[BigDecimal],
                                             taxableProfit: Option[BigDecimal],
                                             netLoss: Option[BigDecimal],
                                             taxableLoss: Option[BigDecimal])

case class RetrieveSelfEmploymentBISSResponse(total: Total, accountingAdjustments: Option[BigDecimal], profit: Option[Profit], loss: Option[Loss]) {
  def toJsonString: String = {
    implicit val formats: Formats = DefaultFormats ++ Seq(BigDecimalSerializer)
    Serialization.write(this)
  }
}

private object BigDecimalSerializer extends CustomSerializer[BigDecimal](format => ( {
  case jde: JDecimal => jde.num
}, {
  case bd: BigDecimal => JDecimal(bd.setScale(2, BigDecimal.RoundingMode.HALF_UP))
}
))

object RetrieveSelfEmploymentBISSResponse {
  implicit val reads: Reads[RetrieveSelfEmploymentBISSResponse] = Json.reads[RetrieveSelfEmploymentDesResponse].map(response =>
    RetrieveSelfEmploymentBISSResponse(
      Total(response.totalIncome, response.totalExpenses, response.totalAdditions, response.totalDeductions),
      response.accountingAdjustments,
      if (response.netProfit.isDefined || response.taxableProfit.isDefined) Some(Profit(response.netProfit, response.taxableProfit)) else None,
      if (response.netLoss.isDefined || response.taxableLoss.isDefined) Some(Loss(response.netLoss, response.taxableLoss)) else None
    )
  )
}

case class Total(income: BigDecimal, expenses: Option[BigDecimal], additions: Option[BigDecimal], deductions: Option[BigDecimal])

case class Profit(net: Option[BigDecimal], taxable: Option[BigDecimal])

case class Loss(net: Option[BigDecimal], taxable: Option[BigDecimal])