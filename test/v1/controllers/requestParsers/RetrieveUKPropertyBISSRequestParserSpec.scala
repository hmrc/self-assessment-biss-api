/*
 * Copyright 2021 HM Revenue & Customs
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

package v1.controllers.requestParsers

import java.time.LocalDate

import support.UnitSpec
import uk.gov.hmrc.domain.Nino
import utils.DateUtils
import v1.mocks.validators.MockRetrieveUKPropertyBISSValidator
import v1.models.des.IncomeSourceType
import v1.models.errors.{BadRequestError, ErrorWrapper, NinoFormatError, TaxYearFormatError}
import v1.models.requestData.{DesTaxYear, RetrieveUKPropertyBISSRawData, RetrieveUKPropertyBISSRequest}

class RetrieveUKPropertyBISSRequestParserSpec extends UnitSpec {

  private val nino = "AA123456B"
  private val taxYear = "2018-19"
  private val typeOfBusinessNonFhl = Some("uk-property-non-fhl")
  private val typeOfBusinessFhl = Some("uk-property-fhl")
  implicit val correlationId: String = "a1e8057e-fbbc-47a8-a8b4-78d9f015c253"

  private val inputData = RetrieveUKPropertyBISSRawData(nino, Some(taxYear), typeOfBusinessNonFhl)
  private val inputDataTwo = RetrieveUKPropertyBISSRawData(nino, Some(taxYear), typeOfBusinessFhl)

  trait Test extends MockRetrieveUKPropertyBISSValidator {
    lazy val parser = new RetrieveUKPropertyBISSRequestDataParser(mockValidator)
  }


  "parse" should {
    "return a request object" when {
      "valid non fhl data is provided" in new Test {
        MockValidator.validate(inputData).returns(Nil)

        parser.parseRequest(inputData) shouldBe Right(RetrieveUKPropertyBISSRequest(Nino(nino), DesTaxYear.fromMtd(taxYear), IncomeSourceType.`uk-property`))
      }

      "valid fhl data is provided" in new Test {
        MockValidator.validate(inputDataTwo).returns(Nil)

        parser.parseRequest(inputDataTwo) shouldBe Right(RetrieveUKPropertyBISSRequest(Nino(nino), DesTaxYear.fromMtd(taxYear), IncomeSourceType.`fhl-property-uk`))
      }
    }

    "valid data is provided without tax year" in new Test {
      MockValidator.validate(inputData.copy(taxYear = None)).returns(Nil)

      parser.parseRequest(inputData.copy(taxYear = None)) shouldBe Right(RetrieveUKPropertyBISSRequest(Nino(nino), DateUtils.getDesTaxYear(LocalDate.now()), IncomeSourceType.`uk-property`))
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