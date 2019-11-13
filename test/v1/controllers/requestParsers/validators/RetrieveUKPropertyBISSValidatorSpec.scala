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
package v1.controllers.requestParsers.validators

import support.UnitSpec
import v1.models.errors.{NinoFormatError, RuleTaxYearNotSupportedError, RuleTaxYearRangeExceededError, TypeOfBusinessFormatError}
import v1.models.requestData.RetrieveUKPropertyBISSRawData

class RetrieveUKPropertyBISSValidatorSpec extends UnitSpec {

  private val nino = "AA123456B"
  private val taxYear = "2018-19"
  private val typeOfBusiness = "uk-property-fhl"
  private val invalidNino = "~~~~~~~~~"
  private val invalidTaxYear = "2018-20"
  private val tooEarlyTaxYear = "2016-17"
  private val invalidTypeOfBusiness = "pigs-with-little-shoes"

  val validator = new RetrieveUKPropertyBISSValidator()


  "running the validator" should {
    "return no errors" when {
      "valid parameters are provided with a tax year" in {
        validator.validate(RetrieveUKPropertyBISSRawData(nino, Some(taxYear), typeOfBusiness)) shouldBe Nil
      }
      "valid parameters are provided with no tax year" in {
        validator.validate(RetrieveUKPropertyBISSRawData(nino, None, typeOfBusiness)) shouldBe Nil
      }
    }

    "return one error" when {
      "an invalid nino is provided" in {
        validator.validate(RetrieveUKPropertyBISSRawData(invalidNino, Some(taxYear), typeOfBusiness)) shouldBe List(NinoFormatError)
      }
      "an invalid tax year is provided" in {
        validator.validate(RetrieveUKPropertyBISSRawData(nino, Some(invalidTaxYear), typeOfBusiness)) shouldBe List(RuleTaxYearRangeExceededError)
      }
      "a too early tax year is provided" in {
        validator.validate(RetrieveUKPropertyBISSRawData(nino, Some(tooEarlyTaxYear), typeOfBusiness)) shouldBe List(RuleTaxYearNotSupportedError)
      }
      "an invalid type of business is provided" in {
        validator.validate(RetrieveUKPropertyBISSRawData(nino, Some(taxYear), invalidTypeOfBusiness)) shouldBe List(TypeOfBusinessFormatError)
      }
    }

    "return multiple errors" when {
      "multiple invalid parameters are provided" in {
        val expectedResult = List(NinoFormatError, TypeOfBusinessFormatError)
        validator.validate(RetrieveUKPropertyBISSRawData(invalidNino, Some(taxYear), invalidTypeOfBusiness)) shouldBe expectedResult
      }
    }
  }
}
