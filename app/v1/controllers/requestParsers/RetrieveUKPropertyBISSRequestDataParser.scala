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

package v1.controllers.requestParsers

import java.time.LocalDate

import javax.inject.Inject
import uk.gov.hmrc.domain.Nino
import utils.DateUtils
import v1.controllers.requestParsers.validators.RetrieveUKPropertyBISSValidator
import v1.models.des.IncomeSourceType
import v1.models.requestData.{RetrieveUKPropertyBISSRawData, RetrieveUKPropertyBISSRequest}

class RetrieveUKPropertyBISSRequestDataParser @Inject()(val validator: RetrieveUKPropertyBISSValidator)
  extends RequestParser[RetrieveUKPropertyBISSRawData, RetrieveUKPropertyBISSRequest] {

  override protected def requestFor(data: RetrieveUKPropertyBISSRawData): RetrieveUKPropertyBISSRequest = {
    RetrieveUKPropertyBISSRequest(Nino(data.nino),
      data.taxYear.fold(DateUtils.getDesTaxYear(LocalDate.now()))(DateUtils.getDesTaxYear),
      (data.typeOfBusiness: @unchecked) match {
        case Some("uk-property-fhl") => IncomeSourceType.`fhl-property-uk`
        case Some("uk-property-non-fhl") => IncomeSourceType.`uk-property`
      })
  }
}
