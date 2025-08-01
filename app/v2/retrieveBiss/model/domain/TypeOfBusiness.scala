/*
 * Copyright 2025 HM Revenue & Customs
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

package v2.retrieveBiss.model.domain

import api.models.downstream.IncomeSourceType
import utils.enums.Enums

enum TypeOfBusiness(val toIncomeSourceType: IncomeSourceType) {
  case `uk-property-non-fhl`      extends TypeOfBusiness(IncomeSourceType.`uk-property`)
  case `uk-property-fhl`          extends TypeOfBusiness(IncomeSourceType.`fhl-property-uk`)
  case `foreign-property`         extends TypeOfBusiness(IncomeSourceType.`foreign-property`)
  case `foreign-property-fhl-eea` extends TypeOfBusiness(IncomeSourceType.`fhl-property-eea`)
  case `self-employment`          extends TypeOfBusiness(IncomeSourceType.`self-employment`)
}

object TypeOfBusiness {
  val parser: PartialFunction[String, TypeOfBusiness] = Enums.parser(values)
}
