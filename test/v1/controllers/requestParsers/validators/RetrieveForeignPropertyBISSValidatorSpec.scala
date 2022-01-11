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

package v1.controllers.requestParsers.validators

import support.UnitSpec
import v1.models.errors.{BusinessIdFormatError, MtdError, NinoFormatError, RuleTaxYearNotSupportedError, RuleTaxYearRangeInvalidError, RuleTypeOfBusinessError, TaxYearFormatError, TypeOfBusinessFormatError}
import v1.models.requestData.RetrieveForeignPropertyBISSRawData

class RetrieveForeignPropertyBISSValidatorSpec extends UnitSpec{

  private val nino = "AA123456B"
  private val invalidNino = "~~~~~~~~~"
  private val businessId = "XAIS12345678901"
  private val validTypeOfBusiness = "foreign-property-fhl-eea"
  private val taxYear = "2019-20"
  private val invalidTaxYear = "2018-28"
  private val invalidTypeOfBusiness = "dog food shop"
  private val noTypeOfBusiness = None
  private val invalidBusinessId = "invalid business id"

  val validator = new RetrieveForeignPropertyBISSValidator()

  "running the validator" should {
    "return no errors" when {
      "valid parameters are provided with a tax year" in {
        validator.validate(RetrieveForeignPropertyBISSRawData(nino, Some(businessId), Some(validTypeOfBusiness), Some(taxYear))) shouldBe Nil
      }
      "valid parameters are provided with no tax year" in {
        validator.validate(RetrieveForeignPropertyBISSRawData(nino, Some(businessId), Some(validTypeOfBusiness), None)) shouldBe Nil
      }
    }

    "return one error" when {
      "an invalid nino is provided" in {
        validator.validate(RetrieveForeignPropertyBISSRawData(invalidNino, Some(businessId), Some(validTypeOfBusiness), Some(taxYear))) shouldBe List(NinoFormatError)
      }
      "an invalid tax year is provided" in {
        validator.validate(RetrieveForeignPropertyBISSRawData(nino, Some(businessId), Some(validTypeOfBusiness), Some(invalidTaxYear))) shouldBe List(RuleTaxYearRangeInvalidError)
      }
      "an invalid type of business is provided" in {
        validator.validate(RetrieveForeignPropertyBISSRawData(nino, Some(businessId), Some(invalidTypeOfBusiness), Some(taxYear))) shouldBe List(TypeOfBusinessFormatError)
      }
      "no type of business is provided" in {
        validator.validate(RetrieveForeignPropertyBISSRawData(nino, Some(businessId), noTypeOfBusiness, Some(taxYear))) shouldBe List(RuleTypeOfBusinessError)
      }
      "an invalid business id is provided" in {
        validator.validate(RetrieveForeignPropertyBISSRawData(nino, Some(invalidBusinessId), Some(validTypeOfBusiness), Some(taxYear))) shouldBe List(BusinessIdFormatError)
      }

    }

    def test(nino: String, businessId: String, typeOfBusiness: String, taxYear: String, error: MtdError): Unit = {
      s"return ${error.code} error" when {
        s"RetrieveForeignPropertyBISSRawData($nino, $businessId, $typeOfBusiness, $taxYear) is supplied" in {
          validator.validate(RetrieveForeignPropertyBISSRawData(nino, Some(businessId), Some(typeOfBusiness), Some(taxYear))) shouldBe List(error)
        }
      }
    }
    Seq(
      ("A12344A", businessId, validTypeOfBusiness, taxYear, NinoFormatError),
      (nino, businessId, validTypeOfBusiness, "201-20", TaxYearFormatError),
      (nino, businessId, validTypeOfBusiness, "2016-17", RuleTaxYearNotSupportedError),
    ).foreach(args => (test _).tupled(args))


  "return multiplpe errors" when {
    "multiple invalid parameters are provided" in {
      val expectedResult = List(NinoFormatError,TypeOfBusinessFormatError)
      validator.validate(RetrieveForeignPropertyBISSRawData(invalidNino,Some(businessId),Some(invalidTypeOfBusiness),Some(taxYear))) shouldBe expectedResult
    }
  }

}}
