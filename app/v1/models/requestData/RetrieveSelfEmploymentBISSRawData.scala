package v1.models.requestData

case class RetrieveSelfEmploymentBISSRawData(nino: String, taxYear: String, selfEmploymentId: String) extends RawData
