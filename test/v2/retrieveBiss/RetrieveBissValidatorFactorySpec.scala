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

package v2.retrieveBiss

import api.controllers.validators.Validator
import support.UnitSpec
import v2.retrieveBiss.def1.Def1_RetrieveBissValidator
import v2.retrieveBiss.model.request.RetrieveBissRequestData

class RetrieveBissValidatorFactorySpec extends UnitSpec {

  private val validNino           = "AA123456A"
  private val validTypeOfBusiness = "uk-property-fhl"
  private val validTaxYear        = "2023-24"
  private val validBusinessId     = "XAIS01234567890"

  val validatorFactory = new RetrieveBissValidatorFactory

  "validator" should {
    "return the parsed domain object" in {
      val result: Validator[RetrieveBissRequestData] = validatorFactory.validator(validNino, validTypeOfBusiness, validTaxYear, validBusinessId)

      result.shouldBe(a[Def1_RetrieveBissValidator])
    }
  }

}
