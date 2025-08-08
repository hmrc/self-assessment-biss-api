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

package v2.retrieveBiss.def1

import api.models.domain.{BusinessId, Nino, TaxYear}
import v2.retrieveBiss.model.domain.TypeOfBusiness
import api.models.errors._
import support.UnitSpec
import v2.retrieveBiss.model.request.Def1_RetrieveBissRequestData

class Def1_RetrieveBissValidatorSpec extends UnitSpec {

  private implicit val correlationId: String = "1234"

  private val validNino           = "AA123456A"
  private val validTypeOfBusiness = "uk-property-fhl"
  private val validTaxYear        = "2023-24"
  private val validBusinessId     = "XAIS01234567890"

  private val parsedNino           = Nino(validNino)
  private val parsedTypeOfBusiness = TypeOfBusiness.parser(validTypeOfBusiness)
  private val parsedTaxYear        = TaxYear.fromMtd(validTaxYear)
  private val parsedBusinessId     = BusinessId(validBusinessId)

  private def validator(nino: String, typeOfBusiness: String, taxYear: String, businessId: String) =
    new Def1_RetrieveBissValidator(nino, typeOfBusiness, taxYear, businessId)

  "validator" should {
    "return the parsed domain object" when {
      "passed a valid request" in {
        val result = validator(validNino, validTypeOfBusiness, validTaxYear, validBusinessId).validateAndWrapResult()

        result.shouldBe(
          Right(
            Def1_RetrieveBissRequestData(parsedNino, parsedTypeOfBusiness, parsedTaxYear, parsedBusinessId)
          ))
      }
    }
  }

  "return a single error" when {
    "passed an invalid nino" in {
      val result = validator("invalid nino", validTypeOfBusiness, validTaxYear, validBusinessId).validateAndWrapResult()

      result.shouldBe(
        Left(
          ErrorWrapper(correlationId, NinoFormatError)
        ))
    }

    "passed an invalid type of business" in {
      val result = validator(validNino, "invalid type of business", validTaxYear, validBusinessId).validateAndWrapResult()

      result.shouldBe(
        Left(
          ErrorWrapper(correlationId, TypeOfBusinessFormatError)
        ))
    }

    "an invalid tax year is provided" in {
      val result = validator(validNino, validTypeOfBusiness, "invalid tax year", validBusinessId).validateAndWrapResult()

      result.shouldBe(
        Left(
          ErrorWrapper(correlationId, TaxYearFormatError)
        ))
    }

    "an invalid tax year range is provided" in {
      val result = validator(validNino, validTypeOfBusiness, "2021-23", validBusinessId).validateAndWrapResult()

      result.shouldBe(
        Left(
          ErrorWrapper(correlationId, RuleTaxYearRangeInvalidError)
        ))
    }

    "given a tax year after 2024-25" in {
      val result = validator(validNino, validTypeOfBusiness, "2025-26", validBusinessId).validateAndWrapResult()
      result.shouldBe(
        Left(
          ErrorWrapper(correlationId, RuleTaxYearNotSupportedError)
        ))
    }

    "an invalid min tax year is provided for UK property" in {
      val result = validator(validNino, validTypeOfBusiness, "2016-17", validBusinessId).validateAndWrapResult()

      result.shouldBe(
        Left(
          ErrorWrapper(correlationId, RuleTaxYearNotSupportedError)
        ))
    }

    "an invalid min tax year is provided for Foreign property" in {
      val result = validator(validNino, "foreign-property", "2018-19", validBusinessId).validateAndWrapResult()

      result.shouldBe(
        Left(
          ErrorWrapper(correlationId, RuleTaxYearNotSupportedError)
        ))
    }

    "passed an invalid business id" in {
      val result = validator(validNino, validTypeOfBusiness, validTaxYear, "invalid business id").validateAndWrapResult()

      result.shouldBe(
        Left(
          ErrorWrapper(correlationId, BusinessIdFormatError)
        ))
    }
  }

  "return multiple errors" when {
    "passed multiple invalid fields" in {
      val result = validator("invalid nino", "invalid type of business", "invalid tax year", "invalid business id").validateAndWrapResult()

      result.shouldBe(
        Left(
          ErrorWrapper(
            correlationId,
            BadRequestError,
            Some(List(NinoFormatError, TaxYearFormatError, TypeOfBusinessFormatError, BusinessIdFormatError))
          )
        ))
    }
  }

}
