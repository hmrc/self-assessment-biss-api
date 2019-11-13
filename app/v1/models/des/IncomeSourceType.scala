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
import utils.enums.Enums
import v1.models.domain.TypeOfBusiness

sealed trait IncomeSourceType {
  def toTypeOfBusiness: TypeOfBusiness
}

object IncomeSourceType {
  case object `uk-property` extends IncomeSourceType {
    override def toTypeOfBusiness: TypeOfBusiness = TypeOfBusiness.`uk-property-non-fhl`
  }

  case object `fhl-property-uk` extends IncomeSourceType {
    override def toTypeOfBusiness: TypeOfBusiness = TypeOfBusiness.`uk-property-fhl`
  }

  implicit val format: Format[IncomeSourceType] = Enums.format[IncomeSourceType]
}
