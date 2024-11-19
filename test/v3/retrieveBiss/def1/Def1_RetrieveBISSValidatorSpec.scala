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

package v3.retrieveBiss.def1

import api.models.domain.{BusinessId, Nino, TaxYear}
import v3.retrieveBiss.model.domain.TypeOfBusiness
import api.models.errors._
import support.UnitSpec
import v3.retrieveBiss.model.request.{Def1_RetrieveBISSRequestData, RetrieveBISSRequestData}

class Def1_RetrieveBISSValidatorSpec extends UnitSpec {

  private implicit val correlationId: String = "1234"

  private val validNino: String = "AA123456A"
  private val validBusinessId: String = "XAIS01234567890"

  private val parsedNino: Nino = Nino(validNino)
  private val parsedBusinessId: BusinessId = BusinessId(validBusinessId)
  private def parsedTypeOfBusiness(typeOfBusiness: String): TypeOfBusiness = TypeOfBusiness.parser(typeOfBusiness)
  private def parsedTaxYear(taxYear: String): TaxYear = TaxYear.fromMtd(taxYear)

  private def validator(nino: String = validNino,
                        typeOfBusiness: String = "uk-property",
                        taxYear: String = "2025-26",
                        businessId: String = validBusinessId): Def1_RetrieveBISSValidator =
    new Def1_RetrieveBISSValidator(nino, typeOfBusiness, taxYear, businessId)

  "Def1_RetrieveBISSValidator" should {
    "return the parsed domain object" when {
      def validTest(typeOfBusiness: String, taxYear: String): Unit =
        s"a valid request for type of business $typeOfBusiness and tax year $taxYear is provided" in {
          val result: Either[ErrorWrapper, RetrieveBISSRequestData] =
            validator(typeOfBusiness = typeOfBusiness, taxYear = taxYear).validateAndWrapResult()

          result shouldBe Right(
            Def1_RetrieveBISSRequestData(
              parsedNino,
              parsedTypeOfBusiness(typeOfBusiness),
              parsedTaxYear(taxYear),
              parsedBusinessId
            )
          )
        }

      val validTestInputs: Seq[(String, String)] = Seq(
        ("uk-property", "2025-26"),
        ("uk-property-fhl", "2024-25"),
        ("self-employment", "2025-26"),
        ("foreign-property-fhl-eea", "2024-25"),
        ("foreign-property", "2025-26")
      )

      validTestInputs.foreach(args => (validTest _).tupled(args))
    }

    "return a single error" when {
      "an invalid nino is provided" in {
        val result: Either[ErrorWrapper, RetrieveBISSRequestData] =
          validator(nino = "invalid nino").validateAndWrapResult()

        result shouldBe Left(
          ErrorWrapper(correlationId, NinoFormatError)
        )
      }

      def typeOfBusinessTest(typeOfBusiness: String, taxYear: String): Unit =
        s"an invalid type of business $typeOfBusiness for tax year $taxYear is provided" in {
          val result: Either[ErrorWrapper, RetrieveBISSRequestData] =
            validator(typeOfBusiness = typeOfBusiness, taxYear = taxYear).validateAndWrapResult()

          result shouldBe Left(
            ErrorWrapper(correlationId, TypeOfBusinessFormatError)
          )
        }

      val typeOfBusinessTestInputs: Seq[(String, String)] = Seq(
        ("uk-property-fhl", "2025-26"),
        ("foreign-property-fhl-eea", "2025-26"),
        ("invalid-type", "2025-26")
      )

      typeOfBusinessTestInputs.foreach(args => (typeOfBusinessTest _).tupled(args))

      "an invalid tax year is provided" in {
        val result: Either[ErrorWrapper, RetrieveBISSRequestData] =
          validator(taxYear = "invalid tax year").validateAndWrapResult()

        result shouldBe Left(
          ErrorWrapper(correlationId, TaxYearFormatError)
        )
      }

      "an invalid tax year range is provided" in {
        val result: Either[ErrorWrapper, RetrieveBISSRequestData] =
          validator(taxYear = "2021-23").validateAndWrapResult()

        result shouldBe Left(
          ErrorWrapper(correlationId, RuleTaxYearRangeInvalidError)
        )
      }

      def minimumTaxYearTest(typeOfBusiness: String, taxYear: String): Unit =
        s"an invalid minimum tax year $taxYear for type of business $typeOfBusiness is provided" in {
          val result: Either[ErrorWrapper, RetrieveBISSRequestData] =
            validator(typeOfBusiness = typeOfBusiness, taxYear = taxYear).validateAndWrapResult()

          result shouldBe Left(
            ErrorWrapper(correlationId, RuleTaxYearNotSupportedError)
          )
        }

      val minimumTaxYearTestInputs: Seq[(String, String)] = Seq(
        ("uk-property", "2016-17"),
        ("uk-property-fhl", "2016-17"),
        ("self-employment", "2016-17"),
        ("foreign-property-fhl-eea", "2018-19"),
        ("foreign-property", "2018-19")
      )

      minimumTaxYearTestInputs.foreach(args => (minimumTaxYearTest _).tupled(args))

      "an invalid business ID is provided" in {
        val result: Either[ErrorWrapper, RetrieveBISSRequestData] =
          validator(businessId = "invalid business id").validateAndWrapResult()

        result shouldBe Left(
          ErrorWrapper(correlationId, BusinessIdFormatError)
        )
      }
    }

    "return multiple errors" when {
      "multiple invalid fields are provided" in {
        val result: Either[ErrorWrapper, RetrieveBISSRequestData] =
          validator("invalid nino", "invalid type of business", "invalid tax year", "invalid business id").validateAndWrapResult()

        result shouldBe Left(
          ErrorWrapper(
            correlationId,
            BadRequestError,
            Some(List(NinoFormatError, TaxYearFormatError, TypeOfBusinessFormatError, BusinessIdFormatError))
          )
        )
      }
    }
  }
}
