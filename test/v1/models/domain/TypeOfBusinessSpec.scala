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

package v1.models.domain

import support.UnitSpec
import utils.enums.EnumJsonSpecSupport
import v1.models.des.IncomeSourceType
import v1.models.domain.TypeOfBusiness._

class TypeOfBusinessSpec extends UnitSpec with EnumJsonSpecSupport {

  testRoundTrip[TypeOfBusiness](("uk-property-non-fhl", `uk-property-non-fhl`), ("uk-property-fhl", `uk-property-fhl`))

  "TypeOfBusiness" should {
    "convert to IncomeSourceType" when {
      "provided uk-property-non-fhl" in {
        TypeOfBusiness.`uk-property-non-fhl`.toIncomeSourceType shouldBe IncomeSourceType.`uk-property`
      }
      "provided uk-property-fhl" in {
        TypeOfBusiness.`uk-property-fhl`.toIncomeSourceType shouldBe IncomeSourceType.`fhl-property-uk`
      }
    }
  }
}