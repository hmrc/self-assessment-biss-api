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
import v1.models.errors.SelfEmploymentIdFormatError

class SelfEmploymentIdValidationSpec extends UnitSpec {

  private val selfEmploymentId = "XAIS12345678901"
  private val invalidSelfEmploymentId = "Actual Beans"

  "validate" should {
    "return no errors" when {
      "a valid self employment id is provided" in {
        SelfEmploymentIdValidation.validate(selfEmploymentId) shouldBe Nil
      }
    }

    "return an error" when {
      "an invalid self employment id is provided" in {
        SelfEmploymentIdValidation.validate(invalidSelfEmploymentId) shouldBe List(SelfEmploymentIdFormatError)
      }
    }
  }

}
