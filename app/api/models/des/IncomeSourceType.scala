/*
 * Copyright 2023 HM Revenue & Customs
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

package api.models.des

sealed trait IncomeSourceType

object IncomeSourceType {
  case object `uk-property` extends IncomeSourceType

  case object `fhl-property-uk` extends IncomeSourceType

  case object `foreign-property` extends IncomeSourceType

  case object `self-employment` extends IncomeSourceType

  case object `fhl-property-eea` extends IncomeSourceType

  case object `01` extends IncomeSourceType

  case object `02` extends IncomeSourceType

  case object `15` extends IncomeSourceType
}
