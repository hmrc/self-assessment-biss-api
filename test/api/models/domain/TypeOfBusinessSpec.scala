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

package api.models.domain

import api.models.des.IncomeSourceType
import api.models.domain.TypeOfBusiness._
import support.UnitSpec
import utils.enums.EnumJsonSpecSupport

class TypeOfBusinessSpec extends UnitSpec with EnumJsonSpecSupport {

  "TypeOfBusiness" should {
    "convert to IncomeSourceType" when {

      testConversion(`uk-property-fhl`, IncomeSourceType.`fhl-property-uk`)
      testConversion(`uk-property-non-fhl`, IncomeSourceType.`uk-property`)
      testConversion(`foreign-property`, IncomeSourceType.`foreign-property`)
      testConversion(`foreign-property-fhl-eea`, IncomeSourceType.`fhl-property-eea`)
      testConversion(`self-employment`, IncomeSourceType.`self-employment`)

      def testConversion(typeOfBusiness: TypeOfBusiness, incomeSourceType: IncomeSourceType): Unit =
        s"provided $typeOfBusiness" in {
          typeOfBusiness.toIncomeSourceType shouldBe incomeSourceType
        }
    }
  }

}
