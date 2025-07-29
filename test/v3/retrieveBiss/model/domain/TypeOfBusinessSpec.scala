/*
 * Copyright 2024 HM Revenue & Customs
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

package v3.retrieveBiss.model.domain

import api.models.des.IncomeSourceType
import v3.retrieveBiss.model.domain.TypeOfBusiness._
import support.UnitSpec

class TypeOfBusinessSpec extends UnitSpec {

  private val validConversions: Seq[(TypeOfBusiness, IncomeSourceType, Int)] = Seq(
    (`self-employment`, IncomeSourceType.`self-employment`, 2025),
    (`uk-property-fhl`, IncomeSourceType.`fhl-property-uk`, 2025),
    (`uk-property`, IncomeSourceType.`uk-property`, 2025),
    (`foreign-property-fhl-eea`, IncomeSourceType.`fhl-property-eea`, 2025),
    (`foreign-property`, IncomeSourceType.`foreign-property`, 2025),
    (`self-employment`, IncomeSourceType.`01`, 2026),
    (`uk-property`, IncomeSourceType.`02`, 2026),
    (`foreign-property`, IncomeSourceType.`15`, 2026)
  )

  private val invalidConversions: Seq[(TypeOfBusiness, IncomeSourceType, Int)] = Seq(
    (`uk-property-fhl`, IncomeSourceType.`fhl-property-uk`, 2026),
    (`foreign-property-fhl-eea`, IncomeSourceType.`fhl-property-eea`, 2026)
  )

  private def testConversion(typeOfBusiness: TypeOfBusiness,
                             incomeSourceType: IncomeSourceType,
                             taxYear: Int,
                             isExpectedToThrowException: Boolean): Unit =
    s"type of business $typeOfBusiness for tax year $taxYear is provided" in {
      if (isExpectedToThrowException) {
        intercept[IllegalArgumentException] {
          typeOfBusiness.toIncomeSourceType(taxYear)
        }.getMessage shouldBe s"Unsupported income source type: $incomeSourceType for tax year: $taxYear"
      } else {
        typeOfBusiness.toIncomeSourceType(taxYear).shouldBe(incomeSourceType)
      }
    }

  "TypeOfBusiness" should {
    "convert to IncomeSourceType" when {
      validConversions.foreach { case (typeOfBusiness, expectedIncomeSourceType, taxYear) =>
        testConversion(typeOfBusiness, expectedIncomeSourceType, taxYear, isExpectedToThrowException = false)
      }
    }

    "not convert to IncomeSourceType and return an IllegalArgumentException" when {
      invalidConversions.foreach { case (typeOfBusiness, expectedIncomeSourceType, taxYear) =>
        testConversion(typeOfBusiness, expectedIncomeSourceType, taxYear, isExpectedToThrowException = true)
      }
    }
  }

}
