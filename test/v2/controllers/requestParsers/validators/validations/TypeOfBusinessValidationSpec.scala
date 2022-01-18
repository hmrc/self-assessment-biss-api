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

package v2.controllers.requestParsers.validators.validations

import support.UnitSpec
import v2.models.errors.TypeOfBusinessFormatError

class TypeOfBusinessValidationSpec extends UnitSpec {

  "validate" should {

    "return no errors" when {
      validate("self-employment")
      validate("uk-property-fhl")
      validate("uk-property-non-fhl")
      validate("foreign-property")
      validate("foreign-property-fhl-eea")

      def validate(typeOfBusiness  : String)  : Unit =
        s"provided with a string of '$typeOfBusiness'" in {
          TypeOfBusinessValidation.validate(typeOfBusiness) shouldBe Nil
        }
    }

    "return a TypeOfBusinessFormatError" when {
      "provided with an unknown type of Business" in {
        TypeOfBusinessValidation.validate("invalid") shouldBe List(TypeOfBusinessFormatError)
      }
    }
  }
}
