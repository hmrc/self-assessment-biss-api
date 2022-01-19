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

package v2.controllers.requestParsers.validators

import support.UnitSpec
import v2.models.errors.{BusinessIdFormatError, NinoFormatError, RuleTaxYearNotSupportedError, RuleTaxYearRangeInvalidError, TaxYearFormatError, TypeOfBusinessFormatError}
import v2.models.requestData.RetrieveBISSRawData

class RetrieveBISSValidatorSpec extends UnitSpec {

  private val nino = "AA123456B"
  private val taxYear = "2021-22"
  private val typeOfBusiness = "uk-property-fhl"
  private val invalidNino = "AA1234B"
  private val businessId = "XAIS12345678910"

  val validator = new RetrieveBISSValidator()


  "running the validator" should {
    "return no errors" when {
      "valid parameters are provided with a tax year" in {
        validator.validate(RetrieveBISSRawData(nino, typeOfBusiness, taxYear, businessId)) shouldBe Nil
      }
    }

    "return error" when {
      "an invalid nino is provided" in {
        validator.validate(RetrieveBISSRawData(invalidNino, typeOfBusiness, taxYear, businessId)) shouldBe List(NinoFormatError)
      }
      "an invalid tax year is provided" in {
        validator.validate(RetrieveBISSRawData(nino, typeOfBusiness, "2020/22", businessId)) shouldBe List(TaxYearFormatError)
      }
      "an invalid tax year range is provided" in {
        validator.validate(RetrieveBISSRawData(nino, typeOfBusiness, "2020-22", businessId)) shouldBe List(RuleTaxYearRangeInvalidError)
      }
      "an invalid min tax year is provided for UK property" in {
        validator.validate(RetrieveBISSRawData(nino, typeOfBusiness, "2016-17", businessId)) shouldBe List(RuleTaxYearNotSupportedError)
      }
      "an invalid min tax year is provided for Foreign property" in {
        validator.validate(RetrieveBISSRawData(nino, "foreign-property", "2018-19", businessId)) shouldBe List(RuleTaxYearNotSupportedError)
      }
      "an invalid type of business is provided" in {
        validator.validate(RetrieveBISSRawData(nino, "self-employments", taxYear, businessId)) shouldBe List(TypeOfBusinessFormatError)
      }
      "an invalid businessId is provided" in {
        validator.validate(RetrieveBISSRawData(nino, typeOfBusiness, taxYear, "XAIS12345678")) shouldBe List(BusinessIdFormatError)
      }
    }

    "return multiple errors" when {
      "multiple invalid parameters are provided" in {
        val expectedResult = List(NinoFormatError, TypeOfBusinessFormatError)
        validator.validate(RetrieveBISSRawData(invalidNino, "invalidTypeOfBusiness", taxYear, businessId)) shouldBe expectedResult
      }
    }
  }
}
