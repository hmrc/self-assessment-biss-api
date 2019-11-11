import play.api.libs.functional.syntax._
import play.api.libs.json._

case class RetrieveSelfEmploymentBISSResponse(total: Total, accountingAdjustments: Option[BigDecimal])

object RetrieveSelfEmploymentBISSResponse {
  implicit val writes: OWrites[RetrieveSelfEmploymentBISSResponse] = Json.writes[RetrieveSelfEmploymentBISSResponse]
  implicit val totalWrites: OWrites[Total] = Json.writes[Total]

  implicit val reads: Reads[RetrieveSelfEmploymentBISSResponse] = (
    (__ \ "totalIncome").read[Total] and
      (__ \ "accountingAdjustments").readNullable[BigDecimal]
    )(RetrieveSelfEmploymentBISSResponse.apply _)

  implicit val totalReads: Reads[Total] = (

  )
}

case class Total(income: BigDecimal, expenses: Option[BigDecimal], additions: Option[BigDecimal], deductions: Option[BigDecimal])