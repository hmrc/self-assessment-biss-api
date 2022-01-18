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

package v2.models.domain

import utils.enums.Enums
import v2.models.des.IncomeSourceType

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

  case object `foreign-property` extends TypeOfBusiness {
    override def toIncomeSourceType: IncomeSourceType = IncomeSourceType.`foreign-property`
  }

  case object `foreign-property-fhl-eea` extends TypeOfBusiness {
    override def toIncomeSourceType: IncomeSourceType = IncomeSourceType.`fhl-property-eea`
  }

  case object `self-employment` extends TypeOfBusiness {
    override def toIncomeSourceType: IncomeSourceType = IncomeSourceType.`self-employment`
  }

  val parser: PartialFunction[String, TypeOfBusiness] = Enums.parser[TypeOfBusiness]
}
