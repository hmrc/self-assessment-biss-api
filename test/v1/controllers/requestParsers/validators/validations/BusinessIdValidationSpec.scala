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

package v1.controllers.requestParsers.validators.validations

import support.UnitSpec
import v1.models.errors.{BusinessIdFormatError, RuleForeignBusinessIdError}

class BusinessIdValidationSpec extends UnitSpec {

  private val businessId = "XAIS12345678901"
  private val invalidBusinessId = "Invalid Business Id"
  private val noBusinessId = None

  "validate" should {
    "return no errors" when {
      "a valid self employment id is provided" in {
        BusinessIdValidation.validate(Some(businessId)) shouldBe Nil
      }
    }

    "return an error" when {
      "an invalid business id is provided" in {
        BusinessIdValidation.validate(Some(invalidBusinessId)) shouldBe List(BusinessIdFormatError)
      }
    }

    "return an error" when {
      "no business id is provided" in {
        BusinessIdValidation.validate(noBusinessId)  shouldBe List(RuleForeignBusinessIdError)
      }
    }



  }

}
