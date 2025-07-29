/*
 * Copyright 2024 HM Revenue & Customs
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

package v3.retrieveBiss.controllers.validators.resolvers

import v3.retrieveBiss.model.domain.TypeOfBusiness
import api.models.errors.TypeOfBusinessFormatError
import cats.data.Validated.{Invalid, Valid}
import support.UnitSpec

class ResolveTypeOfBusinessSpec extends UnitSpec {

  "ResolveTypeOfBusiness" should {

    "return no errors" when {
      resolve("self-employment")
      resolve("uk-property-fhl")
      resolve("uk-property")
      resolve("foreign-property")
      resolve("foreign-property-fhl-eea")

      def resolve(value: String): Unit = {
        s"provided with a string of '$value'" in {
          val result = ResolveTypeOfBusiness(value)
          result shouldBe Valid(TypeOfBusiness.parser(value))
        }
      }
    }

    "return a TypeOfBusinessFormatError" in {
      val result = ResolveTypeOfBusiness("invalid")
      result shouldBe Invalid(List(TypeOfBusinessFormatError))
    }
  }

}
