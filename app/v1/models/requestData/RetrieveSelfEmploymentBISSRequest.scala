package v1.models.requestData

import uk.gov.hmrc.domain.Nino

case class RetrieveSelfEmploymentBISSRequest(nino: Nino, taxYear: DesTaxYear, selfEmploymentId: String)
