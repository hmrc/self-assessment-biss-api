package v1.models.des

import play.api.libs.json._
import support.UnitSpec

class RetrieveSelfEmploymentBISSResponseSpec extends UnitSpec {
  val totalIncome: BigDecimal = 100.00
  val totalExpenses: BigDecimal = 50.00
  val totalAdditions: BigDecimal = 5.00
  val totalDeductions: BigDecimal = 60.00
  val accountingAdjustments: BigDecimal = -30.00
  val netProfit: BigDecimal = 20.00
  val taxableProfit: BigDecimal = 0.00
  val netLoss: BigDecimal = 0.00
  val taxableLoss: BigDecimal = 35.00

  "JSON Reads" should {
    "return a valid case class" when {
      "all fields are provided" in {
        val json =
          Json.parse(
            s"""
               |{
               |  "totalIncome": $totalIncome,
               |  "totalExpenses": $totalExpenses,
               |  "totalAdditions": $totalAdditions,
               |  "totalDeductions": $totalDeductions,
               |  "accountingAdjustments": $accountingAdjustments,
               |  "netProfit": $netProfit,
               |  "taxableProfit": $taxableProfit,
               |  "netLoss": $netLoss,
               |  "taxableLoss": $taxableLoss
               |}
               |""".stripMargin)

        json.as[RetrieveSelfEmploymentBISSResponse] shouldBe RetrieveSelfEmploymentBISSResponse(Total(totalIncome, Some(totalExpenses), Some(totalAdditions), Some(totalDeductions)), Some(accountingAdjustments), Some(Profit(Some(netProfit), Some(taxableProfit))), Some(Loss(Some(netLoss), Some(taxableLoss))))
      }

      "only mandatory fields are provided" in {
        val json =
          Json.parse(
            s"""
               |{
               |  "totalIncome": $totalIncome
               |}
               |""".stripMargin)

        json.as[RetrieveSelfEmploymentBISSResponse] shouldBe RetrieveSelfEmploymentBISSResponse(Total(totalIncome, None, None, None), None, None, None)
      }
    }
  }

  "JSON Writes" should {
    "return valid JSON" when {
      "a case class with all fields present is provided" in {
        val model = RetrieveSelfEmploymentBISSResponse(Total(totalIncome, Some(totalExpenses), Some(totalAdditions), Some(totalDeductions)), Some(accountingAdjustments), Some(Profit(Some(netProfit), Some(taxableProfit))), Some(Loss(Some(netLoss), Some(taxableLoss))))

        Json.toJson(model) shouldBe Json.parse(
          s"""
             |{
             |  "total": {
             |    "income": $totalIncome,
             |    "expenses": $totalExpenses,
             |    "additions": $totalAdditions,
             |    "deductions": $totalDeductions
             |  },
             |  "accountingAdjustments": $accountingAdjustments,
             |  "profit": {
             |    "net": $netProfit,
             |    "taxable": $taxableProfit
             |  },
             |  "loss": {
             |    "net": $netLoss,
             |    "taxable": $taxableLoss
             |  }
             |}
             |""".stripMargin)
      }

      "a case class with only mandatory fields present is provided" in {
        val model = RetrieveSelfEmploymentBISSResponse(Total(totalIncome, None, None, None), None, None, None)

        Json.toJson(model) shouldBe Json.parse(
          s"""
             |{
             |  "total": {
             |    "income": $totalIncome
             |  }
             |}
             |""".stripMargin)
      }
    }
  }

}
