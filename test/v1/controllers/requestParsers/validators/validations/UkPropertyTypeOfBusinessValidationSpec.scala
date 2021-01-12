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

package v1.controllers.requestParsers.validators.validations

import support.UnitSpec
import v1.models.errors.{RuleTypeOfBusinessError, TypeOfBusinessFormatError}

class UkPropertyTypeOfBusinessValidationSpec extends UnitSpec{

  private val typeOfBusiness = Some("uk-property-fhl")
  private val badTypeOfBusiness = Some("rabbit hair comb shop")
  private val noTypeOfBusiness = None

  "validate" should {
    "return no errors" when {
      "a valid type of business is provided" in {
        UKPropertyTypeOfBusinessValidation.validate(typeOfBusiness) shouldBe Nil
      }
    }

    "return a type of business format error" when {
      "an invalid type of business is provided" in {
        UKPropertyTypeOfBusinessValidation.validate(badTypeOfBusiness) shouldBe List(TypeOfBusinessFormatError)
      }
    }

    "return a rule type of business error" when {
      "no type of business is provided" in {
        UKPropertyTypeOfBusinessValidation.validate(noTypeOfBusiness) shouldBe List(RuleTypeOfBusinessError)
      }
    }
  }
}
