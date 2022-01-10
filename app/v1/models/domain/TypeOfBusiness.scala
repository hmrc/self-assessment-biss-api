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

package v1.models.domain

import play.api.libs.json._
import utils.enums.Enums
import v1.models.des.IncomeSourceType

sealed trait TypeOfBusiness {
  def toIncomeSourceType: IncomeSourceType
}

object TypeOfBusiness {
  case object `uk-property-non-fhl` extends TypeOfBusiness {
    override def toIncomeSourceType: IncomeSourceType = IncomeSourceType.`uk-property`
  }

  case object `uk-property-fhl` extends TypeOfBusiness {
    override def toIncomeSourceType: IncomeSourceType = IncomeSourceType.`fhl-property-uk`
  }

  implicit val format: Format[TypeOfBusiness] = Enums.format[TypeOfBusiness]
}
