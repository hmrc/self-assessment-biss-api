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

package v2.controllers.validators

import api.controllers.validators.Validator
import api.controllers.validators.resolvers.{ResolveBusinessId, ResolveNino, ResolveTaxYear, ResolveTypeOfBusiness}
import api.models.errors.MtdError
import cats.data.Validated
import cats.data.Validated._
import cats.implicits._
import v2.controllers.validators.RetrieveBISSValidator.validateBusinessRules
import v2.models.requestData.RetrieveBISSRequestData

import javax.inject.Singleton

@Singleton
class RetrieveBISSValidatorFactory {

  def validator(nino: String, typeOfBusiness: String, taxYear: String, businessId: String): Validator[RetrieveBISSRequestData] =
    new Validator[RetrieveBISSRequestData] {

      def validate: Validated[Seq[MtdError], RetrieveBISSRequestData] =
        (
          ResolveNino(nino),
          ResolveTypeOfBusiness(typeOfBusiness),
          ResolveTaxYear(taxYear),
          ResolveBusinessId(businessId)
        ).mapN(RetrieveBISSRequestData) andThen validateBusinessRules

    }

}
