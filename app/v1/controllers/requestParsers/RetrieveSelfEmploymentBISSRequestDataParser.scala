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

package v1.controllers.requestParsers

import java.time.LocalDate

import javax.inject.Inject
import uk.gov.hmrc.domain.Nino
import utils.DateUtils
import v1.controllers.requestParsers.validators.RetrieveSelfEmploymentBISSValidator
import v1.models.requestData.{RetrieveSelfEmploymentBISSRawData, RetrieveSelfEmploymentBISSRequest}

class RetrieveSelfEmploymentBISSRequestDataParser @Inject()(val validator: RetrieveSelfEmploymentBISSValidator)
  extends RequestParser[RetrieveSelfEmploymentBISSRawData, RetrieveSelfEmploymentBISSRequest] {

  override protected def requestFor(data: RetrieveSelfEmploymentBISSRawData): RetrieveSelfEmploymentBISSRequest =
    RetrieveSelfEmploymentBISSRequest(Nino(data.nino),
      data.taxYear.fold(DateUtils.getDesTaxYear(LocalDate.now()))(DateUtils.getDesTaxYear),
      data.selfEmploymentId)
}
