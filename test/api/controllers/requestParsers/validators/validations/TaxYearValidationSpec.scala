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

package api.controllers.requestParsers.validators.validations

import api.models.errors.{RuleTaxYearNotSupportedError, RuleTaxYearRangeInvalidError, TaxYearFormatError}
import support.UnitSpec
import v2.controllers.requestParsers.validators.validations.TaxYearValidation

class TaxYearValidationSpec extends UnitSpec {

  val minTaxYear = 2018
  val taxYear    = "2017-18"

  "validate" should {
    "return no errors" when {
      "a valid taxYear is supplied" in {
        val validationResult = TaxYearValidation.validate(minTaxYear, taxYear)

        validationResult shouldBe Nil
      }
    }
    "return an error" when {
      "a taxYear with an invalid format is supplied" in {
        val validationResult = TaxYearValidation.validate(minTaxYear, "2019/20")

        validationResult shouldBe List(TaxYearFormatError)
      }
      "a taxYear with a range longer than 1 is supplied" in {
        val validationResult = TaxYearValidation.validate(minTaxYear, "2021-23")

        validationResult shouldBe List(RuleTaxYearRangeInvalidError)
      }
      "a taxYear that isn't the minimum is supplied" in {
        val validationResult = TaxYearValidation.validate(minTaxYear, "2016-17")

        validationResult shouldBe List(RuleTaxYearNotSupportedError)
      }
    }
  }

}