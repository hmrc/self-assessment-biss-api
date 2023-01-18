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

package v2.controllers.requestParsers

import api.models.domain.{Nino, TaxYear, TypeOfBusiness}
import api.models.errors.{BadRequestError, ErrorWrapper, NinoFormatError, TaxYearFormatError}
import support.UnitSpec
import v2.mocks.validators.MockRetrieveBISSValidator
import v2.models.requestData.{RetrieveBISSRawData, RetrieveBISSRequest}

class RetrieveBISSRequestDataParserSpec extends UnitSpec {

  private val nino                      = "AA123456B"
  private val taxYear                   = "2018-19"
  private val taxYearForForeignProperty = "2019-20"
  private val typeOfBusiness            = "uk-property-fhl"
  private val businessId                = "XAIS12345678910"

  implicit val correlationId: String = "a1e8057e-fbbc-47a8-a8b4-78d9f015c253"

  private val inputData = RetrieveBISSRawData(nino, typeOfBusiness, taxYear, businessId)

  trait Test extends MockRetrieveBISSValidator {
    lazy val parser = new RetrieveBISSRequestDataParser(mockValidator)
  }

  "parse" should {
    "return a request object" when {
      "valid uk property data is provided" in new Test {
        MockValidator.validate(inputData).returns(Nil)

        parser.parseRequest(inputData) shouldBe Right(
          RetrieveBISSRequest(Nino(nino), TypeOfBusiness.`uk-property-fhl`, TaxYear.fromMtd(taxYear), businessId))
      }

      "valid foreign property data is provided" in new Test {
        MockValidator.validate(inputData.copy(typeOfBusiness = "foreign-property", taxYear = taxYearForForeignProperty)).returns(Nil)

        parser.parseRequest(inputData.copy(typeOfBusiness = "foreign-property", taxYear = taxYearForForeignProperty)) shouldBe
          Right(RetrieveBISSRequest(Nino(nino), TypeOfBusiness.`foreign-property`, TaxYear.fromMtd(taxYearForForeignProperty), businessId))
      }
    }

    "return an ErrorWrapper" when {
      "a single error is found" in new Test {
        MockValidator.validate(inputData).returns(List(NinoFormatError))

        parser.parseRequest(inputData) shouldBe Left(ErrorWrapper(correlationId, NinoFormatError))
      }

      "a multiple errors are found" in new Test {
        MockValidator.validate(inputData).returns(List(NinoFormatError, TaxYearFormatError))

        parser.parseRequest(inputData) shouldBe Left(ErrorWrapper(correlationId, BadRequestError, Some(List(NinoFormatError, TaxYearFormatError))))
      }
    }
  }

}
