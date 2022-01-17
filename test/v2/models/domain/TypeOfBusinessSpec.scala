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

import support.UnitSpec
import utils.enums.EnumJsonSpecSupport
import v2.models.des.IncomeSourceType
import v2.models.domain.TypeOfBusiness._

class TypeOfBusinessSpec extends UnitSpec with EnumJsonSpecSupport {

  testRoundTrip[TypeOfBusiness](
    "uk-property-non-fhl"      -> `uk-property-non-fhl`,
    "uk-property-fhl"          -> `uk-property-fhl`,
    "foreign-property"         -> `foreign-property`,
    "foreign-property-fhl-eea" -> `foreign-property-fhl-eea`,
    "self-employment"          -> `self-employment`,
  )

  "TypeOfBusiness" should {
    "convert to IncomeSourceType" when {

      testConversion(TypeOfBusiness.`uk-property-fhl`, IncomeSourceType.`fhl-property-uk` )
      testConversion(TypeOfBusiness.`uk-property-non-fhl`, IncomeSourceType.`uk-property` )
      testConversion(TypeOfBusiness.`foreign-property`, IncomeSourceType.`foreign-property` )
      testConversion(TypeOfBusiness.`foreign-property-fhl-eea`, IncomeSourceType.`fhl-property-eea` )
      testConversion(TypeOfBusiness.`self-employment`, IncomeSourceType.`self-employment` )

      def testConversion(typeOfBusiness: TypeOfBusiness, incomeSourceType: IncomeSourceType): Unit =
        s"provided $typeOfBusiness" in {
          typeOfBusiness.toIncomeSourceType shouldBe incomeSourceType
        }
    }
  }
}