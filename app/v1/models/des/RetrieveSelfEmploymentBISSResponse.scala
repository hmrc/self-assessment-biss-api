package v1.models.des

import play.api.libs.json._

case class RetrieveSelfEmploymentDesResponse(totalIncome: BigDecimal, totalExpenses: Option[BigDecimal], totalAdditions: Option[BigDecimal], totalDeductions: Option[BigDecimal], accountingAdjustments: Option[BigDecimal], netProfit: Option[BigDecimal], taxableProfit: Option[BigDecimal], netLoss: Option[BigDecimal], taxableLoss: Option[BigDecimal])

case class RetrieveSelfEmploymentBISSResponse(total: Total, accountingAdjustments: Option[BigDecimal], profit: Option[Profit], loss: Option[Loss])

object RetrieveSelfEmploymentBISSResponse {
  implicit val reads: Reads[RetrieveSelfEmploymentBISSResponse] = Json.reads[RetrieveSelfEmploymentDesResponse].map(response =>
    RetrieveSelfEmploymentBISSResponse(
      Total(response.totalIncome, response.totalExpenses, response.totalAdditions, response.totalDeductions),
      response.accountingAdjustments,
      if(response.netProfit.isDefined || response.taxableProfit.isDefined) Some(Profit(response.netProfit, response.taxableProfit)) else None,
      if(response.netLoss.isDefined || response.taxableLoss.isDefined) Some(Loss(response.netLoss, response.taxableLoss)) else None
    )
  )

  implicit val writesTotal: Writes[Total] = Json.writes[Total]
  implicit val writesProfit: Writes[Profit] = Json.writes[Profit]
  implicit val writesLoss: Writes[Loss] = Json.writes[Loss]
  implicit val writes: Writes[RetrieveSelfEmploymentBISSResponse] = Json.writes[RetrieveSelfEmploymentBISSResponse]
}

case class Total(income: BigDecimal, expenses: Option[BigDecimal], additions: Option[BigDecimal], deductions: Option[BigDecimal])

case class Profit(net: Option[BigDecimal], taxable: Option[BigDecimal])

case class Loss(net: Option[BigDecimal], taxable: Option[BigDecimal])