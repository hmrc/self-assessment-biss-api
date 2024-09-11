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

package v3.retrieveBiss

import api.controllers.validators.Validator
import api.models.errors.MtdError
import cats.data.Validated
import cats.data.Validated.{Invalid, Valid}
import org.scalamock.handlers.CallHandler
import org.scalamock.scalatest.MockFactory
import v3.retrieveBiss.model.request.RetrieveBISSRequestData

trait MockRetrieveBISSValidatorFactory extends MockFactory {

  val mockRetrieveBISSValidatorFactory: RetrieveBISSValidatorFactory = mock[RetrieveBISSValidatorFactory]

  object MockRetrieveBISSValidatorFactory {

    def validator(): CallHandler[Validator[RetrieveBISSRequestData]] =
      (mockRetrieveBISSValidatorFactory.validator(_: String, _: String, _: String, _: String)).expects(*, *, *, *)

  }

  def willUseValidator(use: Validator[RetrieveBISSRequestData]): CallHandler[Validator[RetrieveBISSRequestData]] = {
    MockRetrieveBISSValidatorFactory
      .validator()
      .anyNumberOfTimes()
      .returns(use)
  }

  def returningSuccess(result: RetrieveBISSRequestData): Validator[RetrieveBISSRequestData] =
    new Validator[RetrieveBISSRequestData] {
      def validate: Validated[Seq[MtdError], RetrieveBISSRequestData] = Valid(result)
    }

  def returning(result: MtdError*): Validator[RetrieveBISSRequestData] = returningErrors(result)

  def returningErrors(result: Seq[MtdError]): Validator[RetrieveBISSRequestData] = new Validator[RetrieveBISSRequestData] {
    def validate: Validated[Seq[MtdError], RetrieveBISSRequestData] = Invalid(result)
  }

}
