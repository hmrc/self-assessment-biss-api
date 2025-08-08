/*
 * Copyright 2025 HM Revenue & Customs
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

package v3.retrieveBiss

import api.controllers.validators.Validator
import api.models.errors.MtdError
import cats.data.Validated
import cats.data.Validated.{Invalid, Valid}
import org.scalamock.handlers.CallHandler
import org.scalamock.scalatest.MockFactory
import org.scalatest.TestSuite
import v3.retrieveBiss.model.request.RetrieveBissRequestData

trait MockRetrieveBissValidatorFactory extends TestSuite with MockFactory {

  val mockRetrieveBissValidatorFactory: RetrieveBissValidatorFactory = mock[RetrieveBissValidatorFactory]

  object MockRetrieveBissValidatorFactory {

    def validator(): CallHandler[Validator[RetrieveBissRequestData]] =
      (mockRetrieveBissValidatorFactory.validator(_: String, _: String, _: String, _: String)).expects(*, *, *, *)

  }

  def willUseValidator(use: Validator[RetrieveBissRequestData]): CallHandler[Validator[RetrieveBissRequestData]] = {
    MockRetrieveBissValidatorFactory
      .validator()
      .anyNumberOfTimes()
      .returns(use)
  }

  def returningSuccess(result: RetrieveBissRequestData): Validator[RetrieveBissRequestData] =
    new Validator[RetrieveBissRequestData] {
      def validate: Validated[Seq[MtdError], RetrieveBissRequestData] = Valid(result)
    }

  def returning(result: MtdError*): Validator[RetrieveBissRequestData] = returningErrors(result)

  def returningErrors(result: Seq[MtdError]): Validator[RetrieveBissRequestData] = new Validator[RetrieveBissRequestData] {
    def validate: Validated[Seq[MtdError], RetrieveBissRequestData] = Invalid(result)
  }

}
